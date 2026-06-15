package com.serioussam.vortexos.application.dto;

/** A user's profile payload (both directions). */
public class ProfileDTO {
    private String displayName;
    private String avatar;
    private String settings; // JSON blob

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }
}
