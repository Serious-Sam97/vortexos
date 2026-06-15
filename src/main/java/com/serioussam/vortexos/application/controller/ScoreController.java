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
        String name = this.currentUser.username();
        Score score = new Score();
        score.setOwnerId(this.currentUser.id());
        score.setOwnerName(name);
        score.setInitials(initialsFor(dto.getInitials(), name));
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

    /** The GLOBAL leaderboard for a game — every user's top entries (Arcade public boards). */
    @GetMapping("/global")
    public List<Score> global(@RequestParam("game") String game,
                              @RequestParam(value = "order", defaultValue = "desc") String order) {
        return "asc".equalsIgnoreCase(order)
                ? this.scoreRepository.findTop10ByGameOrderByValueAsc(game)
                : this.scoreRepository.findTop10ByGameOrderByValueDesc(game);
    }

    /** Sanitize client initials to A–Z, max 3; fall back to the first letters of the username. */
    private static String initialsFor(String raw, String username) {
        String cleaned = raw == null ? "" : raw.toUpperCase().replaceAll("[^A-Z]", "");
        if (!cleaned.isBlank()) {
            return cleaned.length() > 3 ? cleaned.substring(0, 3) : cleaned;
        }
        String fallback = username == null ? "" : username.toUpperCase().replaceAll("[^A-Z]", "");
        if (fallback.isBlank()) return "AAA";
        return fallback.length() >= 3 ? fallback.substring(0, 3) : fallback;
    }
}
