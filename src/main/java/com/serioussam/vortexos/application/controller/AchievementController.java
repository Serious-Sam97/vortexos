package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.AchievementUnlockResponse;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.achievement.Achievement;
import com.serioussam.vortexos.infrastructure.repository.JpaAchievementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Per-user achievements. All operations are scoped to the authenticated user. */
@RestController
@RequestMapping("/achievements")
public class AchievementController {
    private final JpaAchievementRepository achievementRepository;
    private final CurrentUser currentUser;

    public AchievementController(JpaAchievementRepository achievementRepository, CurrentUser currentUser) {
        this.achievementRepository = achievementRepository;
        this.currentUser = currentUser;
    }

    /** The keys this user has unlocked. */
    @GetMapping
    public List<Achievement> mine() {
        return this.achievementRepository.findByOwnerId(this.currentUser.id());
    }

    /** Unlock a key (idempotent). 201 if newly unlocked, 200 if the user already had it. */
    @PostMapping("/{key}")
    public ResponseEntity<AchievementUnlockResponse> unlock(@PathVariable("key") String key) {
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Long uid = this.currentUser.id();
        return this.achievementRepository.findByOwnerIdAndAchKey(uid, key)
                .map(a -> ResponseEntity.ok(new AchievementUnlockResponse(a.getAchKey(), a.getUnlockedAt(), false)))
                .orElseGet(() -> {
                    Achievement a = new Achievement();
                    a.setOwnerId(uid);
                    a.setAchKey(key);
                    a.setUnlockedAt(System.currentTimeMillis());
                    Achievement saved = this.achievementRepository.save(a);
                    return new ResponseEntity<>(
                            new AchievementUnlockResponse(saved.getAchKey(), saved.getUnlockedAt(), true),
                            HttpStatus.CREATED);
                });
    }
}
