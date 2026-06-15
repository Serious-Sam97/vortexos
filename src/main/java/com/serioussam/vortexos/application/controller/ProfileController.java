package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.ProfileDTO;
import com.serioussam.vortexos.application.security.CurrentUser;
import com.serioussam.vortexos.domain.profile.Profile;
import com.serioussam.vortexos.infrastructure.repository.JpaProfileRepository;
import org.springframework.web.bind.annotation.*;

/** The authenticated user's cloud profile (identity + settings blob). */
@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final JpaProfileRepository profileRepository;
    private final CurrentUser currentUser;

    public ProfileController(JpaProfileRepository profileRepository, CurrentUser currentUser) {
        this.profileRepository = profileRepository;
        this.currentUser = currentUser;
    }

    /** The caller's profile, defaulting display name to the username if unset. */
    @GetMapping
    public ProfileDTO mine() {
        Long uid = this.currentUser.id();
        Profile p = this.profileRepository.findByOwnerId(uid).orElse(null);
        ProfileDTO dto = new ProfileDTO();
        boolean hasName = p != null && p.getDisplayName() != null && !p.getDisplayName().isBlank();
        dto.setDisplayName(hasName ? p.getDisplayName() : this.currentUser.username());
        dto.setAvatar(p != null ? p.getAvatar() : null);
        dto.setSettings(p != null ? p.getSettings() : "{}");
        return dto;
    }

    /** Upsert the caller's profile. */
    @PutMapping
    public ProfileDTO save(@RequestBody ProfileDTO dto) {
        Long uid = this.currentUser.id();
        Profile p = this.profileRepository.findByOwnerId(uid).orElseGet(Profile::new);
        p.setOwnerId(uid);
        if (dto.getDisplayName() != null) p.setDisplayName(dto.getDisplayName());
        if (dto.getAvatar() != null) p.setAvatar(dto.getAvatar());
        if (dto.getSettings() != null) p.setSettings(dto.getSettings());
        this.profileRepository.save(p);
        return mine();
    }
}
