package com.serioussam.vortexos.application.dto;

/** Create / update an address-book contact. Only `name` is required. */
public record ContactRequest(String name, String email, String phone, String notes) {}
