package com.nhnacademy.observer.impl;

import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.event.handler.ClientEventHandler;
import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.observer.MessageObserver;

import java.util.Map;

public class AuthObserver implements MessageObserver {
    private final ClientEventListener listener;
    private final ClientEventHandler handler;

    public AuthObserver(ClientEventListener listener, ClientEventHandler handler) {
        this.listener = listener;
        this.handler = handler;
    }

    @Override
    public void execute(MessageType type, Map<String, Object> data) {
        switch (type) {
            case LOGIN_SUCCESS -> {
                listener.onLoginSuccess(handler.getMyUserId());
                listener.onShowRoomList();
                handler.loadRoomListScene();
            }
            case LOGIN_FAIL -> {
                String reason = (String) data.getOrDefault("reason", "로그인 실패");
                listener.showError("로그인 실패", reason);
            }
            case LOGOUT_SUCCESS -> listener.onLogout();
        }
    }
}