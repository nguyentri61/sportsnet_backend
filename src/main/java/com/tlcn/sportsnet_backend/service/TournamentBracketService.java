package com.tlcn.sportsnet_backend.service;


import com.tlcn.sportsnet_backend.dto.bracket.*;
import com.tlcn.sportsnet_backend.entity.BracketParticipant;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentMatch;
import com.tlcn.sportsnet_backend.enums.MatchStatus;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentMatchRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
import com.tlcn.sportsnet_backend.repository.TournamentTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentBracketService {
    private final TournamentParticipantRepository participantRepo;
    private final TournamentTeamRepository teamRepo;
    private final TournamentMatchRepository matchRepo;
    private final TournamentCategoryRepository categoryRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerTournamentHistoryService historyService;

    public List<TournamentMatchResponse> generateBracket(String categoryId) {

        TournamentCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<? extends BracketParticipant> participants;

        // loại participant
        String type = category.getCategory().getType();

        if (type.equals("SINGLE")) {
            participants = participantRepo.findByCategory(category);
        } else if (type.equals("DOUBLE")) {
            participants = teamRepo.findByCategory(category);
        } else {
            throw new RuntimeException("Invalid category type");
        }

        if (participants.isEmpty()) {
            throw new RuntimeException("Category has no participants");
        }

        return generateBracketGeneric(category, participants);
    }


    private <T extends BracketParticipant> List<TournamentMatchResponse> generateBracketGeneric(
            TournamentCategory category,
            List<T> participants
    ) {

        int n = participants.size();
        int bracketSize = nextPowerOfTwo(n);

        List<T> list = new ArrayList<>(participants);

        while (list.size() < bracketSize) list.add(null);

        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));

        List<TournamentMatch> allMatches = new ArrayList<>();
        List<T> current = list;

        for (int round = 1; round <= totalRounds; round++) {

            List<T> nextRound = new ArrayList<>();
            int index = 1;

            for (int i = 0; i < current.size(); i += 2) {

                T p1 = current.get(i);
                T p2 = current.get(i + 1);

                TournamentMatch match = TournamentMatch.builder()
                        .category(category)
                        .round(round)
                        .matchIndex(index++)
                        .participant1Id(p1 != null ? p1.getId() : null)
                        .participant2Id(p2 != null ? p2.getId() : null)
                        .participant1Name(p1 != null ? p1.getDisplayName() : null)
                        .participant2Name(p2 != null ? p2.getDisplayName() : null)
                        .status(MatchStatus.NOT_STARTED)
                        .build();

                matchRepo.save(match);
                allMatches.add(match);

                nextRound.add(null);
            }

            current = nextRound;
        }

        return allMatches.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p *= 2;
        return p;
    }

    public TournamentMatchResponse updateMatchResult(String matchId, UpdateMatchResultRequest req) {

        TournamentMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (req.getSets() == null || req.getSets().isEmpty()) {
            throw new InvalidDataException("Set scores are required");
        }

        int winP1 = 0;
        int winP2 = 0;

        List<Integer> setP1 = new ArrayList<>();
        List<Integer> setP2 = new ArrayList<>();

        for (SetScore s : req.getSets()) {
            int p1 = s.getP1();
            int p2 = s.getP2();

            setP1.add(p1);
            setP2.add(p2);

            int winner = getSetWinner(p1, p2);

            if (winner == 1) {
                winP1++;
            } else if (winner == 2) {
                winP2++;
            }

            // best of 3 → ai thắng 2 set thì dừng
            if (winP1 == 2 || winP2 == 2) {
                break;
            }
        }

        // cập nhật điểm (luôn cập nhật)
        match.setSetScoreP1(setP1);
        match.setSetScoreP2(setP2);

        boolean hasWinner = winP1 == 2 || winP2 == 2;

        if (hasWinner) {
            boolean p1Winner = winP1 > winP2;

            String winnerId = p1Winner
                    ? match.getParticipant1Id()
                    : match.getParticipant2Id();

            String winnerName = p1Winner
                    ? match.getParticipant1Name()
                    : match.getParticipant2Name();

            match.setWinnerId(winnerId);
            match.setWinnerName(winnerName);
            match.setStatus(MatchStatus.FINISHED);

            matchRepo.save(match);

            advanceWinner(match);

            // TỰ ĐỘNG GHI HISTORY
            historyService.finishMatchAndSaveHistory(match.getId());

        } else {
            match.setStatus(MatchStatus.IN_PROGRESS);
            matchRepo.save(match);
        }

        TournamentMatchResponse res = convertToResponse(match);

        // luôn bắn socket để FE cập nhật realtime
        messagingTemplate.convertAndSend(
                "/topic/match-updates/" + match.getCategory().getId(),
                res
        );

        return res;
    }

    private int getSetWinner(int p1, int p2) {
        // chưa kết thúc set
        if (p1 < 21 && p2 < 21) return 0;

        // trường hợp chạm 30
        if (p1 == 30 && p2 == 29) return 1;
        if (p2 == 30 && p1 == 29) return 2;

        // >=21 và hơn 2 điểm
        if (p1 >= 21 && p1 - p2 >= 2) return 1;
        if (p2 >= 21 && p2 - p1 >= 2) return 2;

        return 0; // set chưa hợp lệ / chưa kết thúc
    }


    private void advanceWinner(TournamentMatch match) {

        TournamentCategory category = match.getCategory();
        int nextRound = match.getRound() + 1;

        List<TournamentMatch> nextMatches =
                matchRepo.findByCategoryAndRound(category, nextRound);

        if (nextMatches.isEmpty()) return;

        int nextIndex = (int) Math.ceil(match.getMatchIndex() / 2.0);

        TournamentMatch target = nextMatches.stream()
                .filter(m -> m.getMatchIndex() == nextIndex)
                .findFirst()
                .orElse(null);

        if (target == null) return;

        if (match.getMatchIndex() % 2 == 1) {
            target.setParticipant1Id(match.getWinnerId());
            target.setParticipant1Name(match.getWinnerName());
        } else {
            target.setParticipant2Id(match.getWinnerId());
            target.setParticipant2Name(match.getWinnerName());
        }

        matchRepo.save(target);
    }


    public BracketTreeResponse getBracketTree(String categoryId) {

        TournamentCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new InvalidDataException("Category not found"));

        List<TournamentMatch> matches =
                matchRepo.findByCategory(category);

        if (matches.isEmpty()) {
            throw new InvalidDataException("Bracket has not been generated for this category");
        }

        int totalRounds = matches.stream()
                .mapToInt(TournamentMatch::getRound)
                .max()
                .orElse(1);

        List<BracketRoundResponse> rounds = new ArrayList<>();

        for (int roundNum = 1; roundNum <= totalRounds; roundNum++) {

            int finalRoundNum = roundNum;
            List<TournamentMatchResponse> matchResponses = matches.stream()
                    .filter(m -> m.getRound() == finalRoundNum)
                    .sorted(Comparator.comparing(TournamentMatch::getMatchIndex))
                    .map(this::convertToResponse)
                    .toList();

            rounds.add(BracketRoundResponse.builder()
                    .round(roundNum)
                    .matches(matchResponses)
                    .build());
        }

        return BracketTreeResponse.builder()
                .categoryId(categoryId)
                .categoryName(category.getCategory().name())
                .totalRounds(totalRounds)
                .rounds(rounds)
                .build();
    }

    private TournamentMatchResponse convertToResponse(TournamentMatch m) {

        return TournamentMatchResponse.builder()
                .matchId(m.getId())
                .round(m.getRound())
                .matchIndex(m.getMatchIndex())

                .player1Id(m.getParticipant1Id())
                .player2Id(m.getParticipant2Id())

                .player1Name(m.getParticipant1Name())
                .player2Name(m.getParticipant2Name())

                .setScoreP1(m.getSetScoreP1())
                .setScoreP2(m.getSetScoreP2())

                .winnerId(m.getWinnerId())
                .winnerName(m.getWinnerName())

                .status(m.getStatus().name())
                .build();
    }


}
