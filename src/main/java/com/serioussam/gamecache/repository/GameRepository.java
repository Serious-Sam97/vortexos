package com.serioussam.gamecache.repository;

import com.serioussam.gamecache.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
