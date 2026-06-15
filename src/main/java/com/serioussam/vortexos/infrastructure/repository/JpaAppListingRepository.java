package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.store.AppListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaAppListingRepository extends JpaRepository<AppListing, Long> {
    Optional<AppListing> findByAppId(String appId);

    boolean existsByAppId(String appId);

    /** Catalogue, most-installed first (then newest). */
    List<AppListing> findByOrderByInstallsDescUpdatedAtDesc();

    /** A user's published apps, newest update first. */
    List<AppListing> findByAuthorIdOrderByUpdatedAtDesc(Long authorId);
}
