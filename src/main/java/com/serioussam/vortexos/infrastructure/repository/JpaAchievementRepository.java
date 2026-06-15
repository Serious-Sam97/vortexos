package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByOwnerId(Long ownerId);
    Optional<Achievement> findByOwnerIdAndAchKey(Long ownerId, String achKey);
}
