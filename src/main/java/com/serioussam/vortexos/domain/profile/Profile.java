package com.serioussam.vortexos.domain.profile;

import jakarta.persistence.*;

/**
 * A user's cloud profile — identity (display name + avatar) plus a JSON blob of their
 * look/feel settings, so personalization follows them to any browser. One row per user.
 */
@Entity
@Table(name = "profile", uniqueConstraints = @UniqueConstraint(columnNames = "ownerId"))
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column
    private String displayName;

    // Avatar — a short string (emoji or small data-URL).
    @Column(columnDefinition = "text")
    private String avatar;

    // Serialized settings (wallpaper, era, theme, accent, volume, …).
    @Column(columnDefinition = "text")
    private String settings;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }
}
