package com.nhnacademy.model;

import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.session.ClientSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class ChatRoom {
    private final String id; // 방 고유 ID
    private final String name; // 방 이름

    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();
    private final List<StoredMessage> messageLog = Collections.synchronizedList(new ArrayList<>());

    public void addSession(ClientSession session) {
        sessions.add(session);
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
    }

    public void addMessage(String senderId, String message) {
        messageLog.add(new StoredMessage(senderId, message, LocalDateTime.now()));
    }

    public void notifyObservers(BroadcastMessage message) {
        for (ClientSession session : sessions) {
            MessageObserver observer = session.getObserver();
            if (observer != null) {
                observer.onMessage(message);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class StoredMessage {
        private String senderId;
        private String content;
        private LocalDateTime timestamp;
    }
}
