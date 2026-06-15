package com.serioussam.vortexos.application.dto;

/** Compose / send / save-draft a mail. `to` is the recipient's username. */
public record MailRequest(String to, String subject, String body) {}
