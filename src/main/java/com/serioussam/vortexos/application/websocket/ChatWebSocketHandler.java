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
 *                      { "type":"status", "status":"active|idle|away", "activity":"<app>",
 *                        "mstatus":"<msn status>", "psm":"<personal message>" }  (msn fields optional)
 *   server → client : { "type":"presence", "users":[ {"name","status","activity","mstatus","psm"} ... ] }
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
    // Offline-message store (Phase 30 · M3) — messages held for users who weren't connected.
    private final com.serioussam.vortexos.infrastructure.repository.JpaOfflineMessageRepository offlineMessages;
    // Permanent message history (Phase 30 follow-up) — every message persisted for later reload.
    private final com.serioussam.vortexos.infrastructure.repository.JpaMessageRepository messages;

    public ChatWebSocketHandler(com.serioussam.vortexos.infrastructure.repository.JpaOfflineMessageRepository offlineMessages,
                                com.serioussam.vortexos.infrastructure.repository.JpaMessageRepository messages) {
        this.offlineMessages = offlineMessages;
        this.messages = messages;
    }

    /** Persist a 1:1 message to the permanent history. */
    private void persist(String sender, String recipient, String body, long ts) {
        com.serioussam.vortexos.domain.messenger.Message m = new com.serioussam.vortexos.domain.messenger.Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setBody(body);
        m.setCreatedAt(ts);
        this.messages.save(m);
    }

    /** Persist a group message once (the multicast fans the same message out per participant). */
    private void persistGroup(String sender, String groupId, String body, long ts) {
        if (this.messages.existsByGroupIdAndSenderAndCreatedAt(groupId, sender, ts)) return;
        com.serioussam.vortexos.domain.messenger.Message m = new com.serioussam.vortexos.domain.messenger.Message();
        m.setSender(sender);
        m.setGroupId(groupId);
        m.setBody(body);
        m.setCreatedAt(ts);
        this.messages.save(m);
    }

    /** Mutable per-user presence state. */
    static final class Meta {
        volatile String status = "active";
        volatile String activity = "";
        // Messenger (Phase 30): the chosen MSN status + personal message, broadcast in presence.
        volatile String mstatus = "available";
        volatile String psm = "";
    }

    private static String userOf(WebSocketSession session) {
        return (String) session.getAttributes().get("username");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String user = userOf(session);
        this.sessions.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(session);
        this.meta.computeIfAbsent(user, k -> new Meta());
        deliverOfflineMessages(user, session);
        broadcastPresence();
    }

    /** Flush any messages queued while this user was offline, then remove them. */
    private void deliverOfflineMessages(String user, WebSocketSession session) {
        var pending = this.offlineMessages.findByRecipientOrderByCreatedAtAsc(user);
        if (pending.isEmpty()) return;
        for (var om : pending) {
            try {
                send(session, this.mapper.writeValueAsString(Map.of(
                        "type", "msg", "from", om.getSender(), "body", om.getBody(), "ts", om.getCreatedAt())));
            } catch (IOException e) {
                return; // keep them queued if delivery failed mid-flush
            }
        }
        this.offlineMessages.deleteAll(pending);
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
                // Optional Messenger fields — only overwritten when the frame carries them,
                // so OS-level status pings (no Messenger open) don't clear the MSN status/psm.
                if (node.has("mstatus")) {
                    String ms = node.path("mstatus").asText("available");
                    m.mstatus = (ms.isBlank() ? "available" : ms);
                }
                if (node.has("psm")) m.psm = node.path("psm").asText("");
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

        // Arcade match frames (invite / accept / decline / move / resign / rematch …) are
        // relayed POINT-TO-POINT to the named opponent, stamped with the sender. The server
        // stays stateless about matches — all game state lives in the two clients.
        if ("match".equals(type)) {
            String to = node.path("to").asText();
            if (to.isBlank()) return;
            com.fasterxml.jackson.databind.node.ObjectNode out = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            out.put("from", userOf(session));
            sendTo(to, this.mapper.writeValueAsString(out));
            return;
        }

        // Messenger typing / nudge / file-transfer frames (Phase 30) — relayed point-to-point,
        // stamped with the sender, nothing persisted.
        if ("typing".equals(type) || "nudge".equals(type) || "file".equals(type)) {
            String to = node.path("to").asText();
            if (to.isBlank()) return;
            com.fasterxml.jackson.databind.node.ObjectNode out = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            out.put("from", userOf(session));
            sendTo(to, this.mapper.writeValueAsString(out));
            return;
        }

        // Group messages — a client-side multicast (one frame per participant). Relay to the
        // named participant AND persist once to the permanent history (de-duped by ts).
        if ("groupmsg".equals(type)) {
            String to = node.path("to").asText();
            if (to.isBlank()) return;
            String from = userOf(session);
            com.fasterxml.jackson.databind.node.ObjectNode out = (com.fasterxml.jackson.databind.node.ObjectNode) node;
            out.put("from", from);
            sendTo(to, this.mapper.writeValueAsString(out));
            String groupId = node.path("groupId").asText();
            String body = node.path("body").asText();
            long ts = node.path("ts").asLong(System.currentTimeMillis());
            if (!groupId.isBlank() && !body.isEmpty()) persistGroup(from, groupId, body, ts);
            return;
        }

        if (!"msg".equals(type)) return;

        String from = userOf(session);
        String to = node.path("to").asText();
        String body = node.path("body").asText();
        if (to.isBlank() || body.isEmpty()) return;

        // Use the client's send timestamp so the sender's live copy, the recipient's copy and
        // the persisted history all share one ts (lets the client de-dup live vs. history).
        long ts = node.path("ts").asLong(System.currentTimeMillis());
        persist(from, to, body, ts); // permanent history (online or offline)

        Set<WebSocketSession> recipientSessions = this.sessions.get(to);
        if (recipientSessions != null && !recipientSessions.isEmpty()) {
            // Recipient online → deliver live.
            sendTo(to, this.mapper.writeValueAsString(Map.of(
                    "type", "msg", "from", from, "body", body, "ts", ts)));
        } else {
            // Recipient offline → also queue it for the sign-in delivery/toast (Phase 30 · M3).
            com.serioussam.vortexos.domain.messenger.OfflineMessage om =
                    new com.serioussam.vortexos.domain.messenger.OfflineMessage();
            om.setRecipient(to);
            om.setSender(from);
            om.setBody(body);
            om.setCreatedAt(ts);
            this.offlineMessages.save(om);
        }
    }

    /**
     * Send each connected user a presence roster. Invisible users ("Appear offline") are
     * hidden from everyone *except themselves* (Phase 30 · M3), so the roster is tailored
     * per recipient.
     */
    private void broadcastPresence() throws IOException {
        for (Map.Entry<String, Set<WebSocketSession>> entry : this.sessions.entrySet()) {
            String recipient = entry.getKey();
            List<Map<String, String>> users = new ArrayList<>();
            for (String name : this.sessions.keySet()) {
                Meta m = this.meta.getOrDefault(name, new Meta());
                if ("invisible".equals(m.mstatus) && !name.equals(recipient)) continue; // hidden from others
                users.add(Map.of("name", name, "status", m.status, "activity", m.activity,
                        "mstatus", m.mstatus, "psm", m.psm));
            }
            String payload = this.mapper.writeValueAsString(Map.of("type", "presence", "users", users));
            for (WebSocketSession s : entry.getValue()) send(s, payload);
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
