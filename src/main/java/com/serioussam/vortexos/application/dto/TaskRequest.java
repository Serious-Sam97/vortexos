package com.serioussam.vortexos.application.dto;

/** Create / update a to-do task. `dueAt` is an optional epoch-millisecond timestamp. */
public record TaskRequest(String title, boolean done, Long dueAt, int priority, String notes) {}
