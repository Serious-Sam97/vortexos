package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.contact.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaContactRepository extends JpaRepository<Contact, Long> {
    /** All of a user's contacts, alphabetised by name. */
    List<Contact> findByOwnerIdOrderByNameAsc(Long ownerId);

    /** A single contact, but only if it belongs to the given owner. */
    Optional<Contact> findByIdAndOwnerId(Long id, Long ownerId);
}
