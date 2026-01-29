package com.nhnacademy.manager;

import com.nhnacademy.session.ClientSession;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // private SessionManager(){} 를 대체함.
public class SessionManager {
    @Getter
    private static final SessionManager instance = new SessionManager();
    private final Map<String, ClientSession> sessionMap = new ConcurrentHashMap<>();

    public void addSession(String userId, ClientSession session) {
        sessionMap.put(userId, session);
    }

    public void removeSession(String userId) {
        sessionMap.remove(userId);
    }

    public ClientSession getSession(String userId) {
        return sessionMap.get(userId);
    }

    public boolean isLoggedIn(String userId) {
        return sessionMap.containsKey(userId);
    }
}
