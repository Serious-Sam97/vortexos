package com.serioussam.vortexos.application.dto;

/** Inbound payload reporting a finished match from the caller's perspective. */
public class MatchResultRequest {
    private String game;
    private String opponent;
    private boolean won;

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getOpponent() { return opponent; }
    public void setOpponent(String opponent) { this.opponent = opponent; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
}
