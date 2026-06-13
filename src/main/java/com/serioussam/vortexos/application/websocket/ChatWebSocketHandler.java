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
 * The Network "Net Send" server. Tracks who is connected (presence) and routes direct
 * messages between users. Wire protocol (JSON text frames):
 *   client → server : { "type":"msg", "to":"<username>", "body":"<text>" }
 *   server → client : { "type":"presence", "users":[ ... ] }
 *                      { "type":"msg", "from":"<username>", "body":"<text>", "ts":<epochMs> }
 * Nothing is persisted — messages are live only.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    // username → that user's open sessions (a user may have several tabs).
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    private static String userOf(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        this.sessions.computeIfAbsent(userOf(session), k -> ConcurrentHashMap.newKeySet()).add(session);
        broadcastPresence();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        Set<WebSocketSession> set = this.sessions.get(userOf(session));
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) this.sessions.remove(userOf(session));
        }
        broadcastPresence();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonNode node = this.mapper.readTree(message.getPayload());
        if (!"msg".equals(node.path("type").asText())) return;

        String from = userOf(session);
        String to = node.path("to").asText();
        String body = node.path("body").asText();
        if (to.isBlank() || body.isEmpty()) return;

        String payload = this.mapper.writeValueAsString(Map.of(
                "type", "msg", "from", from, "body", body, "ts", System.currentTimeMillis()));
        sendTo(to, payload);
    }

    private void broadcastPresence() throws IOException {
        List<String> online = new ArrayList<>(this.sessions.keySet());
        String payload = this.mapper.writeValueAsString(Map.of("type", "presence", "users", online));
        for (Set<WebSocketSession> set : this.sessions.values()) {
            for (WebSocketSession s : set) send(s, payload);
        }
    }

    private void sendTo(String user, String payload) throws IOException {
        Set<WebSocketSession> set = this.sessions.get(user);
        if (set == null) return;
        for (WebSocketSession s : set) send(s, payload);
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
