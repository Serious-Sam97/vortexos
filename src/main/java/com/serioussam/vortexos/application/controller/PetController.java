package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.PetCardDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.pet.PetCard;
import com.serioussam.vortexos.infrastructure.repository.JpaPetCardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vortex Pet — public pet cards & leaderboard (Phase 29 social layer). Each user mirrors a
 * small snapshot of their tended pet here so others can see it and rank against it. Live
 * pet-to-pet interactions (playdates, gifts, pokes, battles) go over the stateless WS relay,
 * not through this controller.
 */
@RestController
@RequestMapping("/pet")
public class PetController {
    private final JpaPetCardRepository cards;
    private final CurrentUser currentUser;

    public PetController(JpaPetCardRepository cards, CurrentUser currentUser) {
        this.cards = cards;
        this.currentUser = currentUser;
    }

    private static PetCardDTO toDto(PetCard c) {
        PetCardDTO d = new PetCardDTO();
        d.setUsername(c.getUsername());
        d.setName(c.getName());
        d.setStage(c.getStage());
        d.setMood(c.getMood());
        d.setLevel(c.getLevel());
        d.setCareScore(c.getCareScore());
        d.setHappiness(c.getHappiness());
        d.setBond(c.getBond());
        d.setUpdatedAt(c.getUpdatedAt());
        return d;
    }

    /** The caller's own card (404 if they've never published one). */
    @GetMapping("/me")
    public PetCardDTO mine() {
        return this.cards.findByUsername(this.currentUser.username())
                .map(PetController::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Upsert the caller's public pet card (username is always taken from the auth context). */
    @PutMapping("/me")
    public PetCardDTO save(@RequestBody PetCardDTO dto) {
        String me = this.currentUser.username();
        PetCard c = this.cards.findByUsername(me).orElseGet(PetCard::new);
        c.setUsername(me);
        if (dto.getName() != null) c.setName(dto.getName().length() > 16 ? dto.getName().substring(0, 16) : dto.getName());
        if (dto.getStage() != null) c.setStage(dto.getStage());
        if (dto.getMood() != null) c.setMood(dto.getMood());
        c.setLevel(dto.getLevel());
        c.setCareScore(Math.max(0, Math.min(100, dto.getCareScore())));
        c.setHappiness(Math.max(0, Math.min(100, dto.getHappiness())));
        c.setBond(Math.max(0, Math.min(100, dto.getBond())));
        c.setUpdatedAt(System.currentTimeMillis());
        this.cards.save(c);
        return toDto(c);
    }

    /** Top pets by care, for the leaderboard. */
    @GetMapping("/leaderboard")
    public List<PetCardDTO> leaderboard() {
        this.currentUser.username(); // require auth
        return this.cards.findTop50ByOrderByCareScoreDescLevelDesc()
                .stream().map(PetController::toDto).collect(Collectors.toList());
    }

    /**
     * Cards for a set of users — used to render the pets of whoever is currently online.
     * {@code ?users=a,b,c}. Unknown users are simply omitted.
     */
    @GetMapping("/cards")
    public List<PetCardDTO> cards(@RequestParam(name = "users", required = false) String users) {
        this.currentUser.username(); // require auth
        if (users == null || users.isBlank()) return List.of();
        List<String> names = Arrays.stream(users.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if (names.isEmpty()) return List.of();
        return this.cards.findByUsernameIn(names).stream().map(PetController::toDto).collect(Collectors.toList());
    }
}
