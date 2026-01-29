package com.nhnacademy.model;

import com.nhnacademy.session.ClientSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class ChatRoom {
    private final String id; // 방 고유 ID
    private final String name; // 방 이름

    private final Set<ClientSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addSession(ClientSession session) {
        sessions.add(session);
    }

    public void removeSession(ClientSession session) {
        sessions.remove(session);
    }
}
