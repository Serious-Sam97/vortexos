package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.MatchResultRequest;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.match.MatchResult;
import com.serioussam.vortexos.infrastructure.repository.JpaMatchResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Per-user multiplayer match history. Scoped to the authenticated user. */
@RestController
@RequestMapping("/matches")
public class MatchResultController {
    private final JpaMatchResultRepository matchRepository;
    private final CurrentUser currentUser;

    public MatchResultController(JpaMatchResultRepository matchRepository, CurrentUser currentUser) {
        this.matchRepository = matchRepository;
        this.currentUser = currentUser;
    }

    /** The caller's match history, newest first. */
    @GetMapping
    public List<MatchResult> mine() {
        return this.matchRepository.findByOwnerIdOrderByCreatedAtDesc(this.currentUser.id());
    }

    /** Report a finished match from the caller's perspective. */
    @PostMapping
    public ResponseEntity<MatchResult> report(@RequestBody MatchResultRequest dto) {
        if (dto.getGame() == null || dto.getGame().isBlank() || dto.getOpponent() == null || dto.getOpponent().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        MatchResult m = new MatchResult();
        m.setOwnerId(this.currentUser.id());
        m.setGame(dto.getGame());
        m.setOpponent(dto.getOpponent());
        m.setWon(dto.isWon());
        m.setCreatedAt(System.currentTimeMillis());
        return new ResponseEntity<>(this.matchRepository.save(m), HttpStatus.CREATED);
    }
}
