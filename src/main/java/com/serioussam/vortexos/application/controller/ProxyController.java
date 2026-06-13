package com.serioussam.vortexos.application.controller;

import com.serioussam.vortexos.application.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * A fetch-and-reframe proxy for the in-OS Browser. Most sites block being embedded in an
 * iframe (X-Frame-Options / CSP frame-ancestors), so the Browser routes pages through here:
 * the server fetches the page and returns it WITHOUT those headers, so it can be framed.
 *
 * Authenticated by a `token` query param (an iframe can't send an Authorization header), and
 * guarded against SSRF (no http(s) other schemes, no private / loopback hosts).
 */
@RestController
public class ProxyController {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final JwtService jwtService;

    public ProxyController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/proxy")
    public ResponseEntity<String> proxy(@RequestParam("url") String url,
                                        @RequestParam(value = "token", required = false) String token) {
        if (token == null || this.jwtService.extractUsername(token) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (Exception e) {
            return errorPage(400, url, "Invalid URL");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
            return errorPage(400, url, "Only http and https URLs are supported");
        }
        if (isBlockedHost(uri.getHost())) {
            return errorPage(403, url, "That address is not allowed");
        }

        try {
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("User-Agent", "Mozilla/5.0 (VortexOS) Gecko/20100101 Firefox/120.0")
                    .header("Accept", "text/html,application/xhtml+xml,*/*")
                    .timeout(Duration.ofSeconds(12))
                    .GET()
                    .build();
            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

            String contentType = res.headers().firstValue("content-type").orElse("text/html");
            String body = res.body();

            if (contentType.contains("text/html")) {
                // Make relative URLs (assets, links) resolve against the original site.
                String baseTag = "<base href=\"" + uri + "\">";
                if (body.matches("(?is).*<head[^>]*>.*")) {
                    body = body.replaceFirst("(?is)(<head[^>]*>)", "$1" + baseTag);
                } else {
                    body = baseTag + body;
                }
                contentType = "text/html; charset=utf-8";
            }

            // Note: we intentionally do NOT forward X-Frame-Options / CSP, so the page frames.
            return ResponseEntity.ok().header("Content-Type", contentType).body(body);
        } catch (Exception e) {
            return errorPage(502, url, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Block loopback / private / link-local hosts to limit SSRF. */
    private boolean isBlockedHost(String host) {
        if (host == null || host.isBlank() || host.equalsIgnoreCase("localhost")) return true;
        try {
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (addr.isLoopbackAddress() || addr.isAnyLocalAddress()
                        || addr.isSiteLocalAddress() || addr.isLinkLocalAddress()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true; // unresolvable → block
        }
        return false;
    }

    private ResponseEntity<String> errorPage(int status, String url, String message) {
        String safeUrl = url == null ? "" : url.replace("<", "&lt;").replace(">", "&gt;");
        String safeMsg = message == null ? "" : message.replace("<", "&lt;").replace(">", "&gt;");
        String html = "<html><body style='font-family:Tahoma,sans-serif;padding:24px;color:#333'>"
                + "<h2>Couldn't open this page</h2>"
                + "<p style='color:#800000'>" + safeMsg + "</p>"
                + "<p style='color:#666'>" + safeUrl + "</p></body></html>";
        return ResponseEntity.status(status).header("Content-Type", "text/html; charset=utf-8").body(html);
    }
}
