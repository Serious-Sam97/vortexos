package com.serioussam.vortexos.infrastructure.repository;

import com.serioussam.vortexos.domain.messenger.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaMessageRepository extends JpaRepository<Message, Long> {

    /** The 1:1 conversation between two users, most-recent first (cap via Pageable). */
    @Query("select m from Message m where m.groupId is null and " +
           "((m.sender = :a and m.recipient = :b) or (m.sender = :b and m.recipient = :a)) " +
           "order by m.createdAt desc")
    List<Message> conversation(@Param("a") String a, @Param("b") String b, Pageable pageable);

    /** A group's messages, most-recent first (cap via Pageable). */
    List<Message> findByGroupIdOrderByCreatedAtDesc(String groupId, Pageable pageable);

    /** De-dup guard for the group multicast (same message fanned out once per participant). */
    boolean existsByGroupIdAndSenderAndCreatedAt(String groupId, String sender, long createdAt);
}
