package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.pet.PetCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaPetCardRepository extends JpaRepository<PetCard, Long> {
    Optional<PetCard> findByUsername(String username);

    /** The best-cared pets, highest care first then highest level — for the leaderboard. */
    List<PetCard> findTop50ByOrderByCareScoreDescLevelDesc();

    List<PetCard> findByUsernameIn(List<String> usernames);
}
