package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@LoginRequired
public class ListRoomCommand implements Command {
    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        List<ChatRoom> rooms = ChatRoomManager.getInstance().getAllRooms();

        List<Map<String, Object>> roomInfoList = new ArrayList<>();
        for (ChatRoom room : rooms) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put(MessageKey.ROOM_ID, room.getId());
            roomInfo.put(MessageKey.ROOM_NAME, room.getName());
            roomInfo.put("userCount", room.getSessions().size());
            roomInfoList.add(roomInfo);
        }

        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_LIST_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.ROOM_LIST, roomInfoList);

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
            log.info("방 목록 전송 완료: 요청자 -> {}", session.getUserId());
        } catch (IOException e) {
            log.error("방 목록 전송 실패", e);
        }
    }
}
