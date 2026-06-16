package com.serioussam.vortexos.application.dto;

/**
 * A buddy as shown in the Messenger contact list — the username plus the cached identity
 * bits (display name, personal message, display picture) so even offline contacts render
 * richly. Live online/away status is layered on top from the WebSocket presence feed.
 */
public record BuddyDTO(String username, String displayName, String personalMessage, String displayPicture) {}
