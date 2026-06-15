package com.serioussam.vortexos.application.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Collects every text frame the server sends to this client. */
    static class Collector extends TextWebSocketHandler {
        final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
        @Override protected void handleTextMessage(WebSocketSession s, TextMessage m) {
            this.frames.add(m.getPayload());
        }
    }

    private String register(String username) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"username\":\"" + username + "\",\"password\":\"pw\"}";
        ResponseEntity<String> res = this.rest.postForEntity("/auth/register", new HttpEntity<>(body, h), String.class);
        try {
            return this.mapper.readTree(res.getBody()).get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private WebSocketSession connect(String token, Collector collector) throws Exception {
        return new StandardWebSocketClient()
                .execute(collector, "ws://localhost:" + port + "/ws?token=" + token)
                .get(3, TimeUnit.SECONDS);
    }

    /** Wait for a frame matching a predicate, draining the queue. */
    private String await(BlockingQueue<String> q, java.util.function.Predicate<JsonNode> match) throws Exception {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            String frame = q.poll(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            if (frame == null) break;
            JsonNode node = this.mapper.readTree(frame);
            if (match.test(node)) return frame;
        }
        return null;
    }

    @Test
    void rejectsHandshakeWithoutValidToken() {
        Collector c = new Collector();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> connect("not.a.jwt", c));
    }

    @Test
    void broadcastsPresenceAndRoutesDirectMessages() throws Exception {
        String alice = "alice-" + UUID.randomUUID();
        String bob = "bob-" + UUID.randomUUID();
        String aliceToken = register(alice);
        String bobToken = register(bob);

        Collector ac = new Collector();
        Collector bc = new Collector();
        WebSocketSession aliceSession = connect(aliceToken, ac);
        connect(bobToken, bc);

        // Both should see a presence frame listing both users once both are connected.
        String presence = await(bc.frames, n -> n.path("type").asText().equals("presence")
                && contains(n.path("users"), alice) && contains(n.path("users"), bob));
        assertThat(presence).isNotNull();

        // alice → bob direct message.
        aliceSession.sendMessage(new TextMessage(
                "{\"type\":\"msg\",\"to\":\"" + bob + "\",\"body\":\"ping\"}"));

        String msg = await(bc.frames, n -> n.path("type").asText().equals("msg")
                && n.path("from").asText().equals(alice) && n.path("body").asText().equals("ping"));
        assertThat(msg).isNotNull();
    }

    // Presence users are objects { name, status, activity }.
    private static boolean contains(JsonNode arr, String value) {
        if (!arr.isArray()) return false;
        for (JsonNode n : arr) if (n.path("name").asText().equals(value)) return true;
        return false;
    }

    private static JsonNode userEntry(JsonNode arr, String name) {
        if (arr.isArray()) for (JsonNode n : arr) if (n.path("name").asText().equals(name)) return n;
        return null;
    }

    @Test
    void statusUpdatesAreReflectedInPresence() throws Exception {
        String carol = "carol-" + UUID.randomUUID();
        String token = register(carol);
        Collector c = new Collector();
        WebSocketSession session = connect(token, c);

        // Initial presence: carol is "active" with no activity.
        String initial = await(c.frames, n -> n.path("type").asText().equals("presence") && contains(n.path("users"), carol));
        assertThat(initial).isNotNull();

        // Report a status + activity, then expect an updated presence frame.
        session.sendMessage(new TextMessage("{\"type\":\"status\",\"status\":\"idle\",\"activity\":\"Minesweeper\"}"));
        String updated = await(c.frames, n -> {
            JsonNode u = userEntry(n.path("users"), carol);
            return n.path("type").asText().equals("presence") && u != null
                    && u.path("status").asText().equals("idle") && u.path("activity").asText().equals("Minesweeper");
        });
        assertThat(updated).isNotNull();
    }
}
