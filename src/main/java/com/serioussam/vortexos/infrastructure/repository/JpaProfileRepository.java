package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.profile.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByOwnerId(Long ownerId);
}
