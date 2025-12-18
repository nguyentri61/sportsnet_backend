package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.tournament_history.CategoryInfoResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.PlayerTournamentHistoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.RoundHistoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.TournamentInfoResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.MatchStatus;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerTournamentHistoryService {

    private final PlayerTournamentHistoryRepository historyRepo;
    private final TournamentMatchRepository matchRepo;
    private final TournamentParticipantRepository participantRepo;
    private final TournamentTeamRepository teamRepo;
    private final AccountRepository accountRepo;
    private final FileStorageService fileStorageService;

    @Transactional
    public List<PlayerTournamentHistoryResponse> finishMatchAndSaveHistory(String matchId) {

        TournamentMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (match.getStatus() != MatchStatus.FINISHED) {
            throw new RuntimeException("Match is not finished yet");
        }

        // tránh ghi trùng history
        if (historyRepo.existsByMatchId(match.getId())) {
            return List.of(); // hoặc throw nếu muốn strict
        }

        TournamentCategory category = match.getCategory();
        boolean isDouble = category.getCategory().getType().equals("DOUBLE");

        return isDouble
                ? saveHistoryForDouble(match)
                : saveHistoryForSingle(match);
    }

    // ---------------------- SINGLE ----------------------

    private List<PlayerTournamentHistoryResponse> saveHistoryForSingle(TournamentMatch match) {

        String p1Id = match.getParticipant1Id(); // participantId
        String p2Id = match.getParticipant2Id();

        TournamentParticipant tp1 = participantRepo.findById(p1Id)
                .orElseThrow(() -> new RuntimeException("Participant 1 not found"));
        TournamentParticipant tp2 = participantRepo.findById(p2Id)
                .orElseThrow(() -> new RuntimeException("Participant 2 not found"));

        Account acc1 = tp1.getAccount();
        Account acc2 = tp2.getAccount();

        PlayerTournamentHistory h1 = buildSingleHistory(match, acc1, acc2);
        PlayerTournamentHistory h2 = buildSingleHistory(match, acc2, acc1);

        List<PlayerTournamentHistory> saved = historyRepo.saveAll(List.of(h1, h2));

        return saved.stream().map(this::mapToResponse).toList();
    }


    private PlayerTournamentHistory buildSingleHistory(
            TournamentMatch match,
            Account player,
            Account opponent
    ) {

        boolean won = match.getWinnerId().equals(match.getParticipant1Id())
                ? player.getId().equals(opponent.getId()) == false
                : player.getId().equals(match.getParticipant2Id());

        RoundHistory r = RoundHistory.builder()
                .round(match.getRound())
                .opponentId(opponent.getId())
                .opponentName(opponent.getUserInfo().getFullName())
                .won(won)
                .scoreP1(new ArrayList<>(match.getSetScoreP1()))
                .scoreP2(new ArrayList<>(match.getSetScoreP2()))
                .build();

        PlayerTournamentHistory h = PlayerTournamentHistory.builder()
                .player(player)
                .category(match.getCategory())
                .isDouble(false)
                .matchId(match.getId())
                .rounds(new ArrayList<>())
                .build();

        r.setPlayerHistory(h);
        h.getRounds().add(r);

        return h;
    }


    // ---------------------- DOUBLE ----------------------

    private List<PlayerTournamentHistoryResponse> saveHistoryForDouble(TournamentMatch match) {

        String t1Id = match.getParticipant1Id();
        String t2Id = match.getParticipant2Id();

        TournamentTeam team1 = teamRepo.findById(t1Id)
                .orElseThrow(() -> new RuntimeException("Team 1 not found"));
        TournamentTeam team2 = teamRepo.findById(t2Id)
                .orElseThrow(() -> new RuntimeException("Team 2 not found"));

        List<PlayerTournamentHistory> list = new ArrayList<>();

        // team1: player1 + player2
        list.add(buildDoubleHistory(match, team1.getPlayer1(), team2.getPlayer1(), team1));
        list.add(buildDoubleHistory(match, team1.getPlayer2(), team2.getPlayer2(), team1));

        // team2
        list.add(buildDoubleHistory(match, team2.getPlayer1(), team1.getPlayer1(), team2));
        list.add(buildDoubleHistory(match, team2.getPlayer2(), team1.getPlayer2(), team2));

        List<PlayerTournamentHistory> saved = historyRepo.saveAll(list);

        return saved.stream().map(this::mapToResponse).toList();
    }


    private PlayerTournamentHistory buildDoubleHistory(
            TournamentMatch match,
            Account player,
            Account opponent,
            TournamentTeam team
    ) {

        boolean won = match.getWinnerId().equals(team.getId());

        RoundHistory r = RoundHistory.builder()
                .round(match.getRound())
                .opponentId(opponent.getId())
                .opponentName(opponent.getUserInfo().getFullName())
                .won(won)
                .scoreP1(new ArrayList<>(match.getSetScoreP1()))
                .scoreP2(new ArrayList<>(match.getSetScoreP2()))
                .build();

        PlayerTournamentHistory h = PlayerTournamentHistory.builder()
                .player(player)
                .category(match.getCategory())
                .teamId(team.getId())
                .isDouble(true)
                .matchId(match.getId())
                .rounds(new ArrayList<>())
                .build();

        r.setPlayerHistory(h);
        h.getRounds().add(r);

        return h;
    }


    public List<PlayerTournamentHistoryResponse> getHistoryByPlayer(String playerId) {
        List<PlayerTournamentHistory> list = historyRepo.findByPlayerIdOrderByCreatedAtDesc(playerId);

        return list.stream().map(this::mapToResponse).toList();
    }

    private PlayerTournamentHistoryResponse mapToResponse(PlayerTournamentHistory h) {

        TournamentCategory c = h.getCategory();
        Tournament t = c.getTournament();

        return PlayerTournamentHistoryResponse.builder()
                .historyId(h.getId())
                .createdAt(h.getCreatedAt())

                // Tournament
                .tournament(
                        TournamentInfoResponse.builder()
                                .tournamentId(t.getId())
                                .name(t.getName())
                                .location(t.getLocation())
                                .logoUrl(fileStorageService.getFileUrl(t.getLogoUrl(), "/tournament"))
                                .slug(t.getSlug())
                                .startDate(t.getStartDate())
                                .endDate(t.getEndDate())
                                .build()
                )

                // Category
                .category(
                        CategoryInfoResponse.builder()
                                .categoryId(c.getId())
                                .categoryName(c.getCategory().getLabel())
                                .type(c.getCategory().getType())
                                .format(c.getFormat())
                                .minLevel(c.getMinLevel())
                                .maxLevel(c.getMaxLevel())
                                .build()
                )

                .isDouble(h.isDouble())
                .teamId(h.getTeamId())
                .finalRanking(h.getFinalRanking())
                .prize(h.getPrize())
                .rounds(h.getRounds().stream()
                        .map(this::mapRound)
                        .toList())
                .build();
    }

    private RoundHistoryResponse mapRound(RoundHistory r) {
        return RoundHistoryResponse.builder()
                .id(r.getId())
                .round(r.getRound())
                .opponentId(r.getOpponentId())
                .opponentName(r.getOpponentName())
                .won(r.isWon())
                .scoreP1(r.getScoreP1())
                .scoreP2(r.getScoreP2())
                .build();
    }

}
