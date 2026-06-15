package com.serioussam.vortexos.application.dto;

/**
 * Publish (or update) an app in the store. The client sends the manifest summary
 * fields (for the catalogue columns) plus the full .vxapp package as a JSON string.
 * The author is taken from the JWT, never the request.
 */
public record AppPublishRequest(
        String appId,
        String name,
        String version,
        String description,
        String icon,
        String packageJson) {}
