package com.nhnacademy.manager;

import com.nhnacademy.session.ClientSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // private SessionManager(){} 를 대체함.
public class SessionManager {
    @Getter
    private static final SessionManager instance = new SessionManager();
    // ConcurrentHashMap을 통해 여러 스레드가 동시에 접속/해제하더라도 안전하게 데이터를 관리함.
    private final Map<String, ClientSession> sessionMap = new ConcurrentHashMap<>();

    // 로그인 성공시 세션 등록
    public void addSession(String userId, ClientSession session) {
        sessionMap.put(userId, session);
    }

    public void removeSession(String userId) {
        sessionMap.remove(userId);
    }

    // 귓속말 기능 등 특정 클라이언트에게 메시지를 보낼 때 사용됨
    public ClientSession getSession(String userId) {
        return sessionMap.get(userId);
    }

    // 중복 로그인 방지용
    public boolean isLoggedIn(String userId) {
        return sessionMap.containsKey(userId);
    }
}
