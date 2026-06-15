package com.serioussam.vortexos.application.dto;

/** New thread = title + body; reply = body only (title ignored). */
public record BbsRequest(String title, String body) {}
