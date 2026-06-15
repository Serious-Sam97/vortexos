package com.serioussam.vortexos.domain.match;

import jakarta.persistence.*;

/**
 * One finished multiplayer match from the owner's perspective. Each client reports its OWN
 * result at game-end, so a single head-to-head produces two rows (one per player). Powers the
 * Arcade's win/loss record, rating and recent-match history.
 */
@Entity
@Table(name = "match_result")
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String game; // versus game id (tictactoe / connect4 / pong)

    @Column(nullable = false)
    private String opponent;

    @Column(nullable = false)
    private boolean won;

    @Column(nullable = false)
    private long createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getOpponent() { return opponent; }
    public void setOpponent(String opponent) { this.opponent = opponent; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
