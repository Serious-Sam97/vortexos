package com.serioussam.vortexos.domain.bbs;

import jakarta.persistence.*;

/**
 * A post on the shared BBS (Bulletin Board System). A top-level thread has a
 * title and parentId == null; a reply has parentId == its thread's id and no
 * title. Authored by a user but PUBLIC — the whole board is shared by everyone.
 */
@Entity
@Table(name = "bbs_post")
public class BbsPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Author — id + denormalised name (so listing a board doesn't need a user join).
    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorName;

    /** Null for a top-level thread; the thread's id for a reply. */
    @Column
    private Long parentId;

    /** Thread title (top-level posts only). */
    @Column
    private String title;

    @Column(nullable = false, length = 4000)
    private String body;

    @Column(nullable = false)
    private long createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
