package com.serioussam.vortexos.application.dto;

/** A persisted message in a conversation history (Phase 30 follow-up). */
public record MessageDTO(String sender, String recipient, String groupId, String body, long ts) {}
