package com.nhnacademy.model;

import com.nhnacademy.session.ClientSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@RequiredArgsConstructor
public class ChatRoom {
    private final String id; // 방 고유 ID
    private final String name; // 방 이름

    private final Set<ClientSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final List<StoredMessage> messageLog = new CopyOnWriteArrayList<>();

    public void addSession(ClientSession session) {
        sessions.add(session);
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
    }

    public void addMessage(String senderId, String message) {
        messageLog.add(new StoredMessage(senderId, message, LocalDateTime.now()));
    }

    @Getter
    @AllArgsConstructor
    public static class StoredMessage {
        private String senderId;
        private String content;
        private LocalDateTime timestamp;
    }
}
