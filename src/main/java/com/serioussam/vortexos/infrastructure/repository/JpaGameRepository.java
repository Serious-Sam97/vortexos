package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.game.Game;
import com.serioussam.vortexos.domain.game.GameRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaGameRepository extends JpaRepository<Game, Long>, GameRepository {

    @Query("select g from Game g where g.backlog = true and g.ownerId = :ownerId")
    public List<Game> backlogGamesList(@Param("ownerId") Long ownerId);

    @Query("select g from Game g where g.backlog = false and g.ownerId = :ownerId")
    public List<Game> gamesList(@Param("ownerId") Long ownerId);

    /** Ownership-checked lookup — returns empty if the game isn't owned by this user. */
    Optional<Game> findByIdAndOwnerId(Long id, Long ownerId);
}
