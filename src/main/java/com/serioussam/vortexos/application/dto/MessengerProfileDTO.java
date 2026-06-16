package com.serioussam.vortexos.application.dto;

/**
 * The caller's Messenger identity (both directions). `displayName` is read-only here
 * (sourced from the cloud Profile) — to change it, use /profile. The Messenger-owned bits
 * are personalMessage, displayPicture and status.
 */
public class MessengerProfileDTO {
    private String displayName;      // read-only convenience (from Profile)
    private String personalMessage;
    private String displayPicture;
    private String status;           // available | busy | away | brb | lunch | invisible

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPersonalMessage() { return personalMessage; }
    public void setPersonalMessage(String personalMessage) { this.personalMessage = personalMessage; }

    public String getDisplayPicture() { return displayPicture; }
    public void setDisplayPicture(String displayPicture) { this.displayPicture = displayPicture; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
