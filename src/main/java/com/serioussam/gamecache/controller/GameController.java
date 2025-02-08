package com.serioussam.gamecache.controller;

import com.serioussam.gamecache.dto.GameDTO;
import com.serioussam.gamecache.model.Game;
import com.serioussam.gamecache.model.Platform;
import com.serioussam.gamecache.repository.GameRepository;
import com.serioussam.gamecache.repository.PlatformRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;

    public GameController (GameRepository gameRepository, PlatformRepository platformRepository)
    {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
    }

    @GetMapping
    public List<Game> getAllGames()
    {
        return this.gameRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody GameDTO gameResponse)
    {
        Platform platform = this.platformRepository.findById(gameResponse.getPlatformId())
                .orElseThrow(() -> new RuntimeException("Platform not found"));

        Game game = new Game();
        game.setTitle(gameResponse.getTitle());
        game.setStartedDate(LocalDate.now());
        game.setPlatform(platform);

        Game savedGame = gameRepository.save(game);

        return new ResponseEntity<>(savedGame, HttpStatus.CREATED);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable("id") Long gameId, @RequestBody GameDTO gameResponse) {
        Optional<Game> gameOptional = this.gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Game game = gameOptional.get();
        game.setNotes(gameResponse.getNotes());
        this.gameRepository.save(game);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable("id") Long gameId) {
        boolean gameExists = this.gameRepository.existsById(gameId);

        if (!gameExists) {
            return ResponseEntity.notFound().build();
        }

        this.gameRepository.deleteById(gameId);
        return ResponseEntity.noContent().build();
    }
}
