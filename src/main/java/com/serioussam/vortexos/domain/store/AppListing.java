package com.serioussam.vortexos.domain.store;

import jakarta.persistence.*;

/**
 * A published app in the cloud App Store. Authored by a user; the catalogue is
 * PUBLIC (anyone signed in can browse + install). Identified by its manifest id
 * (appId, e.g. "com.sam.notes") — unique, so re-publishing updates in place.
 * The full .vxapp package travels as a JSON string (packageJson).
 */
@Entity
@Table(name = "app_listing")
public class AppListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The manifest id — unique across the store. */
    @Column(nullable = false, unique = true)
    private String appId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    // Author — id + denormalised name (so listing doesn't need a user join).
    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String author;

    @Column(length = 1000)
    private String description;

    /** Icon as a data: URL (text). */
    @Column(columnDefinition = "text")
    private String icon;

    /** The full AppPackage as a JSON string. */
    @Column(columnDefinition = "text", nullable = false)
    private String packageJson;

    @Column(nullable = false)
    private long installs;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getPackageJson() { return packageJson; }
    public void setPackageJson(String packageJson) { this.packageJson = packageJson; }

    public long getInstalls() { return installs; }
    public void setInstalls(long installs) { this.installs = installs; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
