package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.dto.AuthRequest;
import com.serioussam.vortexos.application.dto.AuthResponse;
import com.serioussam.vortexos.application.security.JwtService;
import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.infrastructure.repository.JpaUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            JpaUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        if (isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().build();
        }
        if (this.userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(this.passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setCreatedDate(LocalDate.now());
        this.userRepository.save(user);

        String token = this.jwtService.generateToken(user.getUsername());
        return new ResponseEntity<>(new AuthResponse(token, user.getUsername()), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = this.jwtService.generateToken(request.username());
        return ResponseEntity.ok(new AuthResponse(token, request.username()));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
