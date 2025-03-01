package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.GameDTO;
import com.serioussam.vortexos.domain.game.Game;
import com.serioussam.vortexos.domain.platform.Platform;
import com.serioussam.vortexos.infrastructure.repository.JpaGameRepository;
import com.serioussam.vortexos.infrastructure.repository.JpaPlatformRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {
    private final JpaGameRepository jpaGameRepository;
    private final JpaPlatformRepository jpaPlatformRepository;

    public GameController (JpaGameRepository jpaGameRepository, JpaPlatformRepository jpaPlatformRepository)
    {
        this.jpaGameRepository = jpaGameRepository;
        this.jpaPlatformRepository = jpaPlatformRepository;
    }

    @GetMapping
    public List<Game> getAllGames()
    {
        return this.jpaGameRepository.gamesList();
    }

    @GetMapping("/backlog")
    public List<Game> getAllPendingGames()
    {
        return this.jpaGameRepository.backlogGamesList();
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody GameDTO gameResponse)
    {
        Platform platform = this.jpaPlatformRepository.findById(gameResponse.getPlatformId())
                .orElseThrow(() -> new RuntimeException("Platform not found"));

        Game game = new Game();
        game.setTitle(gameResponse.getTitle());
        game.setStartedDate(LocalDate.now());
        game.setPlatform(platform);
        game.setBacklog(gameResponse.getBacklog());

        Game savedGame = jpaGameRepository.save(game);

        return new ResponseEntity<>(savedGame, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable("id") Long gameId, @RequestBody GameDTO gameResponse) {
        Optional<Game> gameOptional = this.jpaGameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        game.setNotes(gameResponse.getNotes());
        this.jpaGameRepository.save(game);

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Game> completeGame(@PathVariable("id") Long gameId) {
        Optional<Game> gameOptional = this.jpaGameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        game.setCompletedDate(LocalDate.now());
        game.setCompleted(true);
        this.jpaGameRepository.save(game);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable("id") Long gameId) {
        boolean gameExists = this.jpaGameRepository.existsById(gameId);

        if (!gameExists) {
            return ResponseEntity.notFound().build();
        }

        this.jpaGameRepository.deleteById(gameId);
        return ResponseEntity.noContent().build();
    }
}
