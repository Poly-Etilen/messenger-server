package com.nhnacademy.model;

import com.nhnacademy.observer.MessageObserver;
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

    private final Set<MessageObserver> sessions = ConcurrentHashMap.newKeySet();
    private final List<StoredMessage> messageLog = Collections.synchronizedList(new ArrayList<>());

    public void addSession(MessageObserver observer) {
        sessions.add(observer);
    }

    public void removeSession(MessageObserver observer) {
        sessions.remove(observer);
    }

    public void addMessage(String senderId, String message) {
        messageLog.add(new StoredMessage(senderId, message, LocalDateTime.now()));
    }

    public void notifyObservers(BroadcastMessage message) {
        for (MessageObserver observer : sessions) {
            observer.onMessage(message);
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
