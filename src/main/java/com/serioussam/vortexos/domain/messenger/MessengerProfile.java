package com.serioussam.vortexos.domain.messenger;

import jakarta.persistence.*;

/**
 * A user's Messenger identity: their personal message (the line under the display name),
 * display picture (an emoji, preset id, or small data-URL), and last-chosen MSN status
 * (available / busy / away / brb / lunch / invisible). One row per user. The display NAME
 * itself comes from the cloud {@code Profile}; this holds only the Messenger-specific bits.
 */
@Entity
@Table(name = "messenger_profile", uniqueConstraints = @UniqueConstraint(columnNames = "ownerId"))
public class MessengerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(length = 200)
    private String personalMessage;

    // Display picture — a short string (emoji, preset id, or small data-URL).
    @Column(columnDefinition = "text")
    private String displayPicture;

    // Last-chosen status: available | busy | away | brb | lunch | invisible.
    @Column
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getPersonalMessage() { return personalMessage; }
    public void setPersonalMessage(String personalMessage) { this.personalMessage = personalMessage; }

    public String getDisplayPicture() { return displayPicture; }
    public void setDisplayPicture(String displayPicture) { this.displayPicture = displayPicture; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
