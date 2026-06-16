package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.messenger.MessengerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaMessengerProfileRepository extends JpaRepository<MessengerProfile, Long> {
    Optional<MessengerProfile> findByOwnerId(Long ownerId);

    /** Bulk-fetch messenger profiles for a set of owners (to enrich a buddy list). */
    List<MessengerProfile> findByOwnerIdIn(List<Long> ownerIds);
}
