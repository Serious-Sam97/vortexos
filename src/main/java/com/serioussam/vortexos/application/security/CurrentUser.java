package com.serioussam.vortexos.application.security;

import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Resolves the authenticated user (set by {@link JwtAuthFilter}) for the current request. */
@Component
public class CurrentUser {

    private final JpaUserRepository userRepository;

    public CurrentUser(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** The current user's id, or 401 if the request isn't authenticated to a known user. */
    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return this.userRepository.findByUsername(auth.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
