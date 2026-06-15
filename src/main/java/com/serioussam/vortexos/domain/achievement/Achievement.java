package com.serioussam.vortexos.domain.achievement;

import jakarta.persistence.*;

/**
 * A single unlocked achievement for a user. `achKey` is a free-form catalog key defined by the
 * client (e.g. "first_blood", "hat_trick"); one row per (owner, key) — unlocking is idempotent.
 */
@Entity
@Table(name = "achievement", uniqueConstraints = @UniqueConstraint(columnNames = {"ownerId", "achKey"}))
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String achKey;

    /** Epoch milliseconds when it was first unlocked. */
    @Column(nullable = false)
    private long unlockedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getAchKey() { return achKey; }
    public void setAchKey(String achKey) { this.achKey = achKey; }

    public long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(long unlockedAt) { this.unlockedAt = unlockedAt; }
}
