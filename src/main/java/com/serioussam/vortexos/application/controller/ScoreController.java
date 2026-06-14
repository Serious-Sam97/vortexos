package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.ScoreDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.score.Score;
import com.serioussam.vortexos.infrastructure.repository.JpaScoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Per-user game high scores. All operations are scoped to the authenticated user. */
@RestController
@RequestMapping("/scores")
public class ScoreController {
    private final JpaScoreRepository scoreRepository;
    private final CurrentUser currentUser;

    public ScoreController(JpaScoreRepository scoreRepository, CurrentUser currentUser) {
        this.scoreRepository = scoreRepository;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<Score> record(@RequestBody ScoreDTO dto) {
        if (dto.getGame() == null || dto.getGame().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Score score = new Score();
        score.setOwnerId(this.currentUser.id());
        score.setGame(dto.getGame());
        score.setValue(dto.getValue());
        score.setCreatedAt(System.currentTimeMillis());
        return new ResponseEntity<>(this.scoreRepository.save(score), HttpStatus.CREATED);
    }

    /** The user's top entries for a game. order=desc (default, score games) or asc (time games). */
    @GetMapping
    public List<Score> top(@RequestParam("game") String game,
                           @RequestParam(value = "order", defaultValue = "desc") String order) {
        Long uid = this.currentUser.id();
        return "asc".equalsIgnoreCase(order)
                ? this.scoreRepository.findTop10ByOwnerIdAndGameOrderByValueAsc(uid, game)
                : this.scoreRepository.findTop10ByOwnerIdAndGameOrderByValueDesc(uid, game);
    }
}
