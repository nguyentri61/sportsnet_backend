package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.tournament_history.CategoryInfoResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.PlayerTournamentHistoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.RoundHistoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.TournamentInfoResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.MatchStatus;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.persistence.EntityManager;
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
    private final FileStorageService fileStorageService;
    private final TournamentResultRepository resultRepo;
    private final RoundHistoryRepository roundRepo;
    private final TournamentCategoryRepository categoryRepo;

    @Transactional
    public void updateHistoryFromCategoryResult(TournamentCategory category) {

        List<TournamentResult> results = resultRepo.findByCategory(category);
        boolean isDouble = category.getCategory().getType().equals("DOUBLE");

        for (TournamentResult r : results) {

            if (!isDouble) {
                PlayerTournamentHistory h =
                        historyRepo.findByPlayerIdAndCategoryId(
                                r.getParticipant().getAccount().getId(),
                                category.getId()
                        ).orElseThrow();

                h.setFinalRanking(r.getRanking());
                h.setPrize(r.getPrize());

            } else {
                TournamentTeam team = r.getTeam();

                // Player 1
                updateDoublePlayerHistory(
                        team.getPlayer1().getId(),
                        category.getId(),
                        r.getRanking(),
                        r.getPrize()
                );

                // Player 2
                updateDoublePlayerHistory(
                        team.getPlayer2().getId(),
                        category.getId(),
                        r.getRanking(),
                        r.getPrize()
                );
            }
        }
    }

    private void updateDoublePlayerHistory(
            String playerId,
            String categoryId,
            Integer ranking,
            String prize
    ) {
        PlayerTournamentHistory history =
                historyRepo.findByPlayerIdAndCategoryId(playerId, categoryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "History not found for double player: " + playerId
                                )
                        );

        history.setFinalRanking(ranking);
        history.setPrize(prize);
    }


    @Transactional
    public void finishMatch(String matchId) {
        TournamentMatch match = matchRepo.findById(matchId).orElseThrow();

        if (match.getStatus() != MatchStatus.FINISHED) {
            throw new RuntimeException("Match not finished");
        }

        String categoryId = match.getCategory().getId();
        boolean isDouble = match.getCategory().getCategory().getType().equals("DOUBLE");

        if (!isDouble) {
            // SINGLE
            TournamentParticipant tp1 = participantRepo.findById(match.getParticipant1Id()).orElseThrow();
            TournamentParticipant tp2 = participantRepo.findById(match.getParticipant2Id()).orElseThrow();

            saveRoundHistorySingle(match, tp1.getAccount().getId(), tp2.getAccount().getId(), tp2.getAccount().getUserInfo().getFullName(), categoryId);
            saveRoundHistorySingle(match, tp2.getAccount().getId(), tp1.getAccount().getId(), tp1.getAccount().getUserInfo().getFullName(), categoryId);

        } else {
            // DOUBLE
            TournamentTeam team1 = teamRepo.findById(match.getParticipant1Id()).orElseThrow();
            TournamentTeam team2 = teamRepo.findById(match.getParticipant2Id()).orElseThrow();

            String opponentName1 = team2.getPlayer1().getUserInfo().getFullName() + " / " + team2.getPlayer2().getUserInfo().getFullName();
            String opponentName2 = team1.getPlayer1().getUserInfo().getFullName() + " / " + team1.getPlayer2().getUserInfo().getFullName();

            saveRoundHistoryDouble(match, team1.getPlayer1().getId(), team1.getId(), team2.getId(), opponentName1, categoryId);
            saveRoundHistoryDouble(match, team1.getPlayer2().getId(), team1.getId(), team2.getId(), opponentName1, categoryId);
            saveRoundHistoryDouble(match, team2.getPlayer1().getId(), team2.getId(), team1.getId(), opponentName2, categoryId);
            saveRoundHistoryDouble(match, team2.getPlayer2().getId(), team2.getId(), team1.getId(), opponentName2, categoryId);
        }
    }

    private void saveRoundHistorySingle(
            TournamentMatch match,
            String playerId,
            String opponentId,
            String opponentName,
            String categoryId
    ) {
        // ✅ Tìm hoặc tạo history
        PlayerTournamentHistory history = historyRepo
                .findByPlayerIdAndCategoryId(playerId, categoryId)
                .orElseGet(() -> historyRepo.save(
                        PlayerTournamentHistory.builder()
                                .playerId(playerId)
                                .categoryId(categoryId)
                                .isDouble(false)
                                .build()
                ));

        // ✅ Kiểm tra trùng
        if (roundRepo.existsByPlayerHistoryIdAndRound(history.getId(), match.getRound())) {
            return;
        }

        // ✅ Xác định thắng thua
        boolean isP1 = playerId.equals(match.getParticipant1Id());
        boolean won = match.getWinnerId() != null &&
                match.getWinnerId().equals(isP1 ? match.getParticipant1Id() : match.getParticipant2Id());

        // ✅ Lưu round - CHỈ ID, KHÔNG OBJECT
        RoundHistory round = RoundHistory.builder()
                .playerHistoryId(history.getId()) // ✅ CHỈ ID
                .round(match.getRound())
                .opponentId(opponentId)
                .opponentName(opponentName)
                .won(won)
                .scoreP1(match.getSetScoreP1() != null ? new ArrayList<>(match.getSetScoreP1()) : new ArrayList<>())
                .scoreP2(match.getSetScoreP2() != null ? new ArrayList<>(match.getSetScoreP2()) : new ArrayList<>())
                .build();

        roundRepo.save(round);
    }

    private void saveRoundHistoryDouble(
            TournamentMatch match,
            String playerId,
            String teamId,
            String opponentTeamId,
            String opponentName,
            String categoryId
    ) {
        // ✅ Tìm hoặc tạo history
        PlayerTournamentHistory history = historyRepo
                .findByPlayerIdAndCategoryId(playerId, categoryId)
                .orElseGet(() -> historyRepo.save(
                        PlayerTournamentHistory.builder()
                                .playerId(playerId)
                                .categoryId(categoryId)
                                .teamId(teamId)
                                .isDouble(true)
                                .build()
                ));

        // ✅ Kiểm tra trùng
        if (roundRepo.existsByPlayerHistoryIdAndRound(history.getId(), match.getRound())) {
            return;
        }

        // ✅ Xác định thắng
        boolean won = match.getWinnerId() != null && match.getWinnerId().equals(teamId);

        // ✅ Lưu round - CHỈ ID
        RoundHistory round = RoundHistory.builder()
                .playerHistoryId(history.getId()) // ✅ CHỈ ID
                .round(match.getRound())
                .opponentId(opponentTeamId)
                .opponentName(opponentName)
                .won(won)
                .scoreP1(match.getSetScoreP1() != null ? new ArrayList<>(match.getSetScoreP1()) : new ArrayList<>())
                .scoreP2(match.getSetScoreP2() != null ? new ArrayList<>(match.getSetScoreP2()) : new ArrayList<>())
                .build();

        roundRepo.save(round);
    }

    public List<PlayerTournamentHistoryResponse> getHistoryByPlayer(String playerId) {
        List<PlayerTournamentHistory> list = historyRepo.findByPlayerIdOrderByCreatedAtDesc(playerId);

        return list.stream().map(this::mapToResponse).toList();
    }

    private PlayerTournamentHistoryResponse mapToResponse(PlayerTournamentHistory h) {

        TournamentCategory c = categoryRepo.findById(h.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Tournament t = c.getTournament();

        List<RoundHistoryResponse> roundResponses =
                roundRepo.findByPlayerHistoryIdOrderByRoundAsc(h.getId())
                        .stream()
                        .map(this::mapRound)
                        .toList();

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
                                .minLevel(c.getMinLevel())
                                .maxLevel(c.getMaxLevel())
                                .build()
                )

                .isDouble(h.isDouble())
                .teamId(h.getTeamId())
                .finalRanking(h.getFinalRanking())
                .prize(h.getPrize())
                .rounds(roundResponses)
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
