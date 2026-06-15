package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.score.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaScoreRepository extends JpaRepository<Score, Long> {
    // Top entries for one user + game, highest first (score games) or lowest first (time games).
    List<Score> findTop10ByOwnerIdAndGameOrderByValueDesc(Long ownerId, String game);
    List<Score> findTop10ByOwnerIdAndGameOrderByValueAsc(Long ownerId, String game);

    // GLOBAL leaderboard — every user's top entries for a game (Arcade public boards).
    List<Score> findTop10ByGameOrderByValueDesc(String game);
    List<Score> findTop10ByGameOrderByValueAsc(String game);
}
