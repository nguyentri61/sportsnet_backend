package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.player_rating.PlayerRatingCreateRequest;
import com.tlcn.sportsnet_backend.dto.player_rating.PlayerRatingResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.PlayerRating;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.PlayerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerRatingService {
    private final AccountRepository accountRepository;
    private final PlayerRatingRepository playerRatingRepository;
    public PlayerRatingResponse createPlayerRating(PlayerRatingCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        PlayerRating playerRating = playerRatingRepository.findByAccount(account)
                .orElse(new PlayerRating());
        playerRating.setExperience(request.getExperience());
        playerRating.setDoubles(request.getDoubles());
        playerRating.setDefense(request.getDefense());
        playerRating.setNetShot(request.getNetShot());
        playerRating.setFootwork(request.getFootwork());
        playerRating.setDropShot(request.getDropShot());
        playerRating.setStamina(request.getStamina());
        playerRating.setTactics(request.getTactics());
        playerRating.setSmash(request.getSmash());
        playerRating.setDrive(request.getDrive());
        playerRating.setClear(request.getClear());
        playerRating.setServe(request.getServe());
        playerRating.setAccount(account);
        playerRating =playerRatingRepository.save(playerRating);
        return toPlayerRatingResponse(playerRating);

    }

    public PlayerRatingResponse getPlayerRating() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        PlayerRating playerRating = playerRatingRepository.findByAccount(account)
                .orElse(null);

        if (playerRating == null) {
            return null;
        }
        return toPlayerRatingResponse(playerRating);
    }

    public PlayerRatingResponse toPlayerRatingResponse(PlayerRating playerRating) {
        return PlayerRatingResponse.builder()
                .id(playerRating.getId())
                .experience(playerRating.getExperience())
                .doubles(playerRating.getDoubles())
                .defense(playerRating.getDefense())
                .netShot(playerRating.getNetShot())
                .footwork(playerRating.getFootwork())
                .dropShot(playerRating.getDropShot())
                .stamina(playerRating.getStamina())
                .tactics(playerRating.getTactics())
                .smash(playerRating.getSmash())
                .drive(playerRating.getDrive())
                .clear(playerRating.getClear())
                .serve(playerRating.getServe())
                .averageTechnicalScore(playerRating.getAverageTechnicalScore())
                .overallScore(playerRating.getOverallScore())
                .skillLevel(playerRating.getSkillLevel())
                .build();
    }


}
