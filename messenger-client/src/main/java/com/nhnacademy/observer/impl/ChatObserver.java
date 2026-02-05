package com.nhnacademy.observer.impl;

import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.event.handler.ClientEventHandler;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.request.RequestFactory;

import java.util.List;
import java.util.Map;

public class ChatObserver implements MessageObserver {
    private final ClientEventListener listener;
    private final ClientEventHandler handler;

    public ChatObserver(ClientEventListener listener, ClientEventHandler handler) {
        this.listener = listener;
        this.handler = handler;
    }

    @Override
    public void execute(MessageType type, Map<String, Object> data) {
        switch (type) {
            case CHAT_MESSAGE -> {
                listener.writeMessage((String) data.get("message"));
                if ("System".equals(data.get("senderId"))) {
                    handler.sendRequest(RequestFactory.roomMemberListRequest(handler.getRoomId()));
                }
            }
            case PRIVATE_MESSAGE_RECEIVE ->
                    listener.writeMessage("Whisper [" + data.get("senderId") + "] : " + data.get("content"));

            case PRIVATE_MESSAGE_SUCCESS ->
                    listener.writeMessage("Whisper [to " + data.get("receiverId") + "] : " + data.get("content"));

            case PUSH_NEW_MESSAGE -> listener.writeMessage((String) data.get("content"));

            case PUSH_ROOM_ENTER -> {
                listener.writeMessage("[알림] " + data.get("userName") + " 님이 입장하셨습니다.");
                handler.sendRequest(RequestFactory.roomMemberListRequest(handler.getRoomId()));
            }
            case PUSH_ROOM_EXIT -> {
                listener.writeMessage("[알림] " + data.get("userId") + " 님이 퇴장하셨습니다.");
                handler.sendRequest(RequestFactory.roomMemberListRequest(handler.getRoomId()));
            }
            case CHAT_MESSAGE_HISTORY_SUCCESS -> handleHistory(data);
        }
    }

    private void handleHistory(Map<String, Object> data) {
        if (!handler.getRoomId().equals(data.get("roomId"))) return;
        List<Map<String, String>> history = (List<Map<String, String>>) data.get("history");
        listener.writeMessage("--- 채팅 히스토리 ---");
        for (Map<String, String> chat : history) {
            listener.writeMessage(chat.get("timestamp") + " " + chat.get("message"));
        }
    }
}
