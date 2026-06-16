package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.messenger.Buddy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaBuddyRepository extends JpaRepository<Buddy, Long> {
    /** A user's whole buddy list, alphabetised. */
    List<Buddy> findByOwnerIdOrderByBuddyUsernameAsc(Long ownerId);

    /** A specific buddy entry, if it exists for this owner. */
    Optional<Buddy> findByOwnerIdAndBuddyUsername(Long ownerId, String buddyUsername);

    boolean existsByOwnerIdAndBuddyUsername(Long ownerId, String buddyUsername);
}
