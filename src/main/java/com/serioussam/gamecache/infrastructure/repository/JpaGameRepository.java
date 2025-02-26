package com.serioussam.gamecache.infrastructure.repository;

import com.serioussam.gamecache.domain.game.Game;
import com.serioussam.gamecache.domain.game.GameRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaGameRepository extends JpaRepository<Game, Long>, GameRepository {

    @Query("select * from Game where pending = true")
    public List<Game> pendingGamesList();

    @Query("select * from Game where pending = false")
    public List<Game> gamesList();
}
