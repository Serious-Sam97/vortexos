package com.serioussam.vortexos.application.dto;

/** A public view of a user — id + username only, never the password hash. */
public record UserSummary(Long id, String username) {
}
