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
}
