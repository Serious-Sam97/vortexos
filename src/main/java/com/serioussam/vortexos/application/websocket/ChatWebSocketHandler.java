package com.serioussam.vortexos.application.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Network server. Tracks who is connected with rich presence (status + the app they're
 * using) and routes direct messages between users. Wire protocol (JSON text frames):
 *   client → server : { "type":"msg", "to":"<username>", "body":"<text>" }
 *                      { "type":"status", "status":"active|idle|away", "activity":"<app>" }
 *   server → client : { "type":"presence", "users":[ {"name","status","activity"} ... ] }
 *                      { "type":"msg", "from":"<username>", "body":"<text>", "ts":<epochMs> }
 * Nothing is persisted — presence and messages are live only.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    // username → that user's open sessions (a user may have several tabs).
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    // username → live presence meta (status + current activity), shared across their tabs.
    private final Map<String, Meta> meta = new ConcurrentHashMap<>();

    /** Mutable per-user presence state. */
    static final class Meta {
        volatile String status = "active";
        volatile String activity = "";
    }

    private static String userOf(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String user = userOf(session);
        this.sessions.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(session);
        this.meta.computeIfAbsent(user, k -> new Meta());
        broadcastPresence();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        String user = userOf(session);
        Set<WebSocketSession> set = this.sessions.get(user);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                this.sessions.remove(user);
                this.meta.remove(user); // last tab gone → no longer present
            }
        }
        broadcastPresence();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonNode node = this.mapper.readTree(message.getPayload());
        String type = node.path("type").asText();

        if ("status".equals(type)) {
            Meta m = this.meta.get(userOf(session));
            if (m != null) {
                String s = node.path("status").asText("active");
                m.status = (s.isBlank() ? "active" : s);
                m.activity = node.path("activity").asText("");
                broadcastPresence();
            }
            return;
        }

        // Shared-surface signals (cursors, drawing strokes, doc edits) are relayed to
        // everyone else, stamped with the sender. The `room` lets clients filter to the
        // shared space they're in; the server stays stateless about rooms.
        if ("cursor".equals(type) || "draw".equals(type) || "edit".equals(type)) {
            com.fasterxml.jackson.databind.node.ObjectNode out = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            out.put("from", userOf(session));
            broadcastToOthers(session, this.mapper.writeValueAsString(out));
            return;
        }

        if (!"msg".equals(type)) return;

        String from = userOf(session);
        String to = node.path("to").asText();
        String body = node.path("body").asText();
        if (to.isBlank() || body.isEmpty()) return;

        String payload = this.mapper.writeValueAsString(Map.of(
                "type", "msg", "from", from, "body", body, "ts", System.currentTimeMillis()));
        sendTo(to, payload);
    }

    private void broadcastPresence() throws IOException {
        List<Map<String, String>> users = new ArrayList<>();
        for (String name : this.sessions.keySet()) {
            Meta m = this.meta.getOrDefault(name, new Meta());
            users.add(Map.of("name", name, "status", m.status, "activity", m.activity));
        }
        String payload = this.mapper.writeValueAsString(Map.of("type", "presence", "users", users));
        for (Set<WebSocketSession> set : this.sessions.values()) {
            for (WebSocketSession s : set) send(s, payload);
        }
    }

    /** Relay a shared-surface frame to every session except the sender's own. */
    private void broadcastToOthers(WebSocketSession sender, String payload) {
        for (Set<WebSocketSession> set : this.sessions.values()) {
            for (WebSocketSession s : set) {
                if (s != sender) send(s, payload);
            }
        }
    }

    private void sendTo(String user, String payload) throws IOException {
        Set<WebSocketSession> set = this.sessions.get(user);
        if (set == null) return;
        for (WebSocketSession s : set) send(s, payload);
    }

    /**
     * Push a JSON-serializable payload to a user's open sessions (e.g. a new-mail ping
     * from VortexMail). Best-effort — a no-op if the user isn't currently connected.
     */
    public void sendToUser(String username, Object payload) {
        Set<WebSocketSession> set = this.sessions.get(username);
        if (set == null) return;
        try {
            String json = this.mapper.writeValueAsString(payload);
            for (WebSocketSession s : set) send(s, json);
        } catch (IOException e) {
            // best-effort push
        }
    }

    private void send(WebSocketSession session, String payload) {
        if (!session.isOpen()) return;
        try {
            // sendMessage isn't safe for concurrent calls on one session — serialize per session.
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException e) {
            // A session that died mid-broadcast shouldn't break delivery to everyone else.
        }
    }
}
