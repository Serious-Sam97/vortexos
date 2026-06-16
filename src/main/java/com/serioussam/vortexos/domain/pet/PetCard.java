package com.serioussam.vortexos.domain.pet;

import jakarta.persistence.*;

/**
 * A user's public Vortex Pet "card" — a small, shareable snapshot of their tended pet (name,
 * life stage, level, current mood and a 0..100 care score). One row per user, keyed by
 * username so it can be looked up and ranked on the pet leaderboard. The authoritative pet
 * state still lives client-side (per-user localStorage); this is just the social mirror.
 */
@Entity
@Table(name = "pet_card", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class PetCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(length = 16)
    private String name;

    // egg | baby | teen | adult
    @Column
    private String stage;

    // available | playful | sleeping | … (the pet's current mood)
    @Column
    private String mood;

    @Column
    private int level;

    @Column
    private int careScore;

    @Column
    private int happiness;

    @Column
    private int bond;

    @Column
    private long updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCareScore() { return careScore; }
    public void setCareScore(int careScore) { this.careScore = careScore; }

    public int getHappiness() { return happiness; }
    public void setHappiness(int happiness) { this.happiness = happiness; }

    public int getBond() { return bond; }
    public void setBond(int bond) { this.bond = bond; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
