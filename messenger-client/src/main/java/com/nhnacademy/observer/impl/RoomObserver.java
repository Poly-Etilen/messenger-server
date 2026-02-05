package com.nhnacademy.observer.impl;

import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.event.handler.ClientEventHandler;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.request.RequestFactory;

import java.util.List;
import java.util.Map;

public class RoomObserver implements MessageObserver {
    private final ClientEventListener listener;
    private final ClientEventHandler handler;

    public RoomObserver(ClientEventListener listener, ClientEventHandler handler) {
        this.listener = listener;
        this.handler = handler;
    }

    @Override
    public void execute(MessageType type, Map<String, Object> data) {
        switch (type) {
            case CHAT_ROOM_LIST_SUCCESS -> {
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
                listener.updateRoomList(rooms);
            }
            case CHAT_ROOM_ENTER_SUCCESS, CHAT_ROOM_CREATE_SUCCESS -> {
                String roomId = (String) data.get("roomId");
                handler.setRoomId(roomId);
                listener.onEnterRoom();
                handler.sendRequest(RequestFactory.roomMemberListRequest(roomId));
            }
            case CHAT_ROOM_EXIT_SUCCESS -> {
                listener.onShowRoomList();
                handler.loadRoomListScene();
            }
            case USER_LIST_SUCCESS -> {
                List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("userList");
                listener.updateUserList(users);
            }
            case CHAT_ROOM_USER_LIST_SUCCESS -> {
                if (handler.getRoomId().equals(data.get("roomId"))) {
                    listener.updateRoomUserList((List<String>) data.get("userList"));
                }
            }
        }
    }
}
