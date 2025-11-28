package com.tlcn.sportsnet_backend.service;


import com.tlcn.sportsnet_backend.dto.bracket.BracketRoundResponse;
import com.tlcn.sportsnet_backend.dto.bracket.BracketTreeResponse;
import com.tlcn.sportsnet_backend.dto.bracket.TournamentMatchResponse;
import com.tlcn.sportsnet_backend.dto.bracket.UpdateMatchResultRequest;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentMatch;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.enums.MatchStatus;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentMatchRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentBracketService {
    private final TournamentParticipantRepository participantRepo;
    private final TournamentMatchRepository matchRepo;
    private final TournamentCategoryRepository categoryRepo;

    public List<TournamentMatchResponse> generateBracket(String categoryId) {

        TournamentCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<TournamentParticipant> participants =
                participantRepo.findByCategory(category);

        if (participants.isEmpty()) {
            throw new RuntimeException("Category does not have participants");
        }

        int n = participants.size();
        int bracketSize = nextPowerOfTwo(n);

        List<TournamentParticipant> list = new ArrayList<>(participants);

        // add BYE
        while (list.size() < bracketSize) list.add(null);

        int totalRounds = (int) (Math.log(bracketSize) / Math.log(2));

        List<TournamentMatch> allMatches = new ArrayList<>();
        List<TournamentParticipant> current = list;

        for (int round = 1; round <= totalRounds; round++) {

            List<TournamentParticipant> nextRound = new ArrayList<>();
            int index = 1;

            for (int i = 0; i < current.size(); i += 2) {

                TournamentParticipant p1 = current.get(i);
                TournamentParticipant p2 = current.get(i + 1);

                TournamentMatch match = TournamentMatch.builder()
                        .category(category)
                        .round(round)
                        .matchIndex(index++)
                        .player1(p1)
                        .player2(p2)
                        .status(MatchStatus.NOT_STARTED)
                        .build();

                matchRepo.save(match);
                allMatches.add(match);

                // slot cho vòng sau
                nextRound.add(null);
            }

            current = nextRound;
        }

        return allMatches.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p *= 2;
        return p;
    }


    public TournamentMatch updateMatchResult(String matchId, UpdateMatchResultRequest req) {

        TournamentMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setScoreP1(req.getScoreP1());
        match.setScoreP2(req.getScoreP2());

        TournamentParticipant winner = null;

        if (req.getWinnerId() != null) {
            winner = participantRepo.findById(req.getWinnerId())
                    .orElseThrow(() -> new RuntimeException("Winner not found"));
        }

        match.setWinner(winner);
        match.setStatus(MatchStatus.FINISHED);
        matchRepo.save(match);

        // ⬇ advance winner into next round
        advanceWinner(match);

        return match;
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
            target.setPlayer1(match.getWinner());
        } else {
            target.setPlayer2(match.getWinner());
        }

        matchRepo.save(target);
    }

    public BracketTreeResponse getBracketTree(String categoryId) {

        TournamentCategory category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new InvalidDataException("Category not found"));

        List<TournamentMatch> matches = matchRepo.findAll()
                .stream()
                .filter(m -> m.getCategory().getId().equals(categoryId))
                .toList();

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

                .player1Id(m.getPlayer1() != null ? m.getPlayer1().getId() : null)
                .player2Id(m.getPlayer2() != null ? m.getPlayer2().getId() : null)

                .player1Name(m.getPlayer1() != null ?
                        m.getPlayer1().getAccount().getUserInfo().getFullName() : null)
                .player2Name(m.getPlayer2() != null ?
                        m.getPlayer2().getAccount().getUserInfo().getFullName() : null)

                .scoreP1(m.getScoreP1())
                .scoreP2(m.getScoreP2())

                .winnerId(m.getWinner() != null ? m.getWinner().getId() : null)
                .winnerName(m.getWinner() != null ?
                        m.getWinner().getAccount().getUserInfo().getFullName() : null)

                .status(m.getStatus().name())
                .build();
    }


}
