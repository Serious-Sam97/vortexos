package com.serioussam.vortexos.domain.score;

import jakarta.persistence.*;

/**
 * A per-user game high-score entry. `game` is a free-form key (e.g. "solitaire",
 * "minesweeper:expert", "snake") and `value` is the metric — higher-is-better for score games,
 * lower-is-better for time games; the client decides which order to request.
 */
@Entity
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owning user's id — a plain column (not a @ManyToOne) to keep the User out of JSON.
    @Column(nullable = false)
    private Long ownerId;

    // Denormalized username + classic 3-letter arcade initials, for the GLOBAL leaderboard
    // (so a global query needs no User join). Nullable: pre-Arcade rows have neither.
    @Column
    private String ownerName;

    @Column(length = 3)
    private String initials;

    @Column(nullable = false)
    private String game;

    @Column(nullable = false)
    private long value;

    /** Epoch milliseconds when the score was recorded. */
    @Column(nullable = false)
    private long createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
