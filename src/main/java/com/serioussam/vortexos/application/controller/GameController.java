package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.GameDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
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
    private final JpaGameRepository gameRepository;
    private final JpaPlatformRepository platformRepository;
    private final CurrentUser currentUser;

    public GameController(JpaGameRepository gameRepository, JpaPlatformRepository platformRepository, CurrentUser currentUser)
    {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<Game> getAllGames()
    {
        return this.gameRepository.gamesList(this.currentUser.id());
    }

    @GetMapping("/backlog")
    public List<Game> getAllPendingGames()
    {
        return this.gameRepository.backlogGamesList(this.currentUser.id());
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody GameDTO gameResponse)
    {
        if (gameResponse.getTitle() == null || gameResponse.getTitle().isBlank() || gameResponse.getPlatformId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Platform> platform = this.platformRepository.findById(gameResponse.getPlatformId());
        if (platform.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = new Game();
        game.setOwnerId(this.currentUser.id());
        game.setTitle(gameResponse.getTitle());
        game.setStartedDate(LocalDate.now());
        game.setPlatform(platform.get());
        game.setBacklog(gameResponse.getBacklog());

        Game savedGame = gameRepository.save(game);

        return new ResponseEntity<>(savedGame, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable("id") Long gameId, @RequestBody GameDTO gameResponse) {
        Optional<Game> gameOptional = this.gameRepository.findByIdAndOwnerId(gameId, this.currentUser.id());

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        game.setNotes(gameResponse.getNotes());
        this.gameRepository.save(game);

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Game> completeGame(@PathVariable("id") Long gameId) {
        Optional<Game> gameOptional = this.gameRepository.findByIdAndOwnerId(gameId, this.currentUser.id());

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        game.setCompletedDate(LocalDate.now());
        game.setCompleted(true);
        this.gameRepository.save(game);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable("id") Long gameId) {
        Optional<Game> gameOptional = this.gameRepository.findByIdAndOwnerId(gameId, this.currentUser.id());

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.gameRepository.delete(gameOptional.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/start")
    public ResponseEntity<Void> startGame(@PathVariable("id") Long gameId) {
        Optional<Game> gameOptional = this.gameRepository.findByIdAndOwnerId(gameId, this.currentUser.id());

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        game.setStartedDate(LocalDate.now());
        game.setNotes("");
        game.setBacklog(false);
        this.gameRepository.save(game);

        return ResponseEntity.noContent().build();
    }
}
