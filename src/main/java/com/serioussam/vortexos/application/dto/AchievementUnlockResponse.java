package com.serioussam.vortexos.application.dto;

/** Result of an unlock attempt — newlyUnlocked is false if the user already had it. */
public record AchievementUnlockResponse(String key, long unlockedAt, boolean newlyUnlocked) {
}
