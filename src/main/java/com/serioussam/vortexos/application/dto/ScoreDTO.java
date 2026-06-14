package com.serioussam.vortexos.application.dto;

/** Inbound payload for recording a score. */
public class ScoreDTO {
    private String game;
    private long value;

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
}
