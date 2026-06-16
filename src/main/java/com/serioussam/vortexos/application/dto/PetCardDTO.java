package com.serioussam.vortexos.application.dto;

/** A public Vortex Pet card payload (both directions). */
public class PetCardDTO {
    private String username;
    private String name;
    private String stage;
    private String mood;
    private int level;
    private int careScore;
    private int happiness;
    private int bond;
    private long updatedAt;

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
