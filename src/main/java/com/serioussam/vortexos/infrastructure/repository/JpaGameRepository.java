package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.game.Game;
import com.serioussam.vortexos.domain.game.GameRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaGameRepository extends JpaRepository<Game, Long>, GameRepository {

    @Query("select g from Game g where g.backlog = true")
    public List<Game> backlogGamesList();

    @Query("select g from Game g where g.backlog = false")
    public List<Game> gamesList();
}
