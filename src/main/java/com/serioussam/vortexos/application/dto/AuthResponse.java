package com.serioussam.vortexos.application.dto;

/** Issued on successful register / login. Never includes the password hash. */
public record AuthResponse(String token, String username) {
}
