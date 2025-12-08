package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.tournament_history.CategoryInfoResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.PlayerTournamentHistoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament_history.TournamentInfoResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.repository.PlayerTournamentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerTournamentHistoryService {
    private final PlayerTournamentHistoryRepository historyRepo;

    /**
     * Ghi lịch sử sau khi 1 trận đấu kết thúc → KHÔNG set ranking
     */
    public void saveHistoryAfterMatch(TournamentMatch match) {

        if (!match.getStatus().name().equals("FINISHED")) return;

        List<PlayerTournamentHistory> list = new ArrayList<>();

        saveForParticipant(list, match, match.getParticipant1Id(), match.getParticipant1Name(), false);
        saveForParticipant(list, match, match.getParticipant2Id(), match.getParticipant2Name(), false);

        historyRepo.saveAll(list);
    }

    private void saveForParticipant(
            List<PlayerTournamentHistory> list,
            TournamentMatch match,
            String pId,
            String pName,
            boolean isDouble
    ) {
        PlayerTournamentHistory history = PlayerTournamentHistory.builder()
                .player(Account.builder().id(pId).build()) // không fetch DB để tối ưu
                .category(match.getCategory())
                .isDouble(isDouble)
                .rounds(Collections.singletonList(
                        RoundHistory.builder()
                                .round(match.getRound())
                                .opponentName(pName)
                                .won(Objects.equals(match.getWinnerId(), pId))
                                .scoreP1(match.getSetScoreP1())
                                .scoreP2(match.getSetScoreP2())
                                .build()
                ))
                .build();

        list.add(history);
    }

    /**
     * Ghi lịch sử sau khi cả giải kết thúc → Gán ranking, điểm thưởng
     */
    public void saveHistoryAfterTournament(TournamentCategory category,
                                           Map<String, Integer> rankingMap,
                                           Map<String, Double> ratingChangeMap) {

        for (TournamentParticipant participant : category.getParticipants()) {
            saveFinalResultSingle(category, participant, rankingMap, ratingChangeMap);
        }

        for (TournamentTeam team : category.getTeams()) {
            saveFinalResultDouble(category, team, rankingMap, ratingChangeMap);
        }
    }

    private void saveFinalResultSingle(
            TournamentCategory category,
            TournamentParticipant p,
            Map<String, Integer> rankingMap,
            Map<String, Double> ratingMap
    ) {
        PlayerTournamentHistory h = PlayerTournamentHistory.builder()
                .player(p.getAccount())
                .category(category)
                .isDouble(false)
                .finalRanking(rankingMap.get(p.getId()))
                .prize(getPrizeByRanking(category, rankingMap.get(p.getId())))
                .build();

        historyRepo.save(h);

    }

    private void saveFinalResultDouble(
            TournamentCategory category,
            TournamentTeam team,
            Map<String, Integer> rankingMap,
            Map<String, Double> ratingMap
    ) {
        Integer ranking = rankingMap.get(team.getId());

        List<Account> players = List.of(team.getPlayer1(), team.getPlayer2());

        for (Account acc : players) {

            PlayerTournamentHistory h = PlayerTournamentHistory.builder()
                    .player(acc)
                    .category(category)
                    .teamId(team.getId())
                    .isDouble(true)
                    .finalRanking(ranking)
                    .prize(getPrizeByRanking(category, ranking))
                    .build();

            historyRepo.save(h);
        }
    }

    private String getPrizeByRanking(TournamentCategory c, Integer r) {
        if (r == null) return null;
        return switch (r) {
            case 1 -> c.getFirstPrize();
            case 2 -> c.getSecondPrize();
            case 3 -> c.getThirdPrize();
            default -> null;
        };
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
                                .logoUrl(t.getLogoUrl())
                                .bannerUrl(t.getBannerUrl())
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
                .rounds(h.getRounds())
                .build();
    }

}
