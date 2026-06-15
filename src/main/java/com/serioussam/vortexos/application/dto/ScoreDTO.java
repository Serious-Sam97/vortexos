package com.serioussam.vortexos.application.dto;

/** Inbound payload for recording a score. */
public class ScoreDTO {
    private String game;
    private long value;
    private String initials; // optional 3-letter arcade initials for the global board

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }

    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
}
