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

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
