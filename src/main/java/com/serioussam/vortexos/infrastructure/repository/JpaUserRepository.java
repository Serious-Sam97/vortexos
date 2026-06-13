package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.user.User;
import com.serioussam.vortexos.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
