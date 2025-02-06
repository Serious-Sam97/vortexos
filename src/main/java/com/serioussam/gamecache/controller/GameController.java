package com.serioussam.gamecache.controller;

import com.serioussam.gamecache.model.Game;
import com.serioussam.gamecache.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {
    private final GameRepository gameRepository;

    public GameController (GameRepository gameRepository)
    {
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public List<Game> getAllGames()
    {
        return this.gameRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Game game)
    {
        Game savedGame = gameRepository.save(game);

        return new ResponseEntity<>(savedGame, HttpStatus.CREATED);
    }
}
