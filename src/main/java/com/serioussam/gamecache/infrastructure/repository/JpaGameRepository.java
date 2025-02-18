package com.serioussam.gamecache.infrastructure.repository;

import com.serioussam.gamecache.domain.game.Game;
import com.serioussam.gamecache.domain.game.GameRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaGameRepository extends JpaRepository<Game, Long>, GameRepository {
}
