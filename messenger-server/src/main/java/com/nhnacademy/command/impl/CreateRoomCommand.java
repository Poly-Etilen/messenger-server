package com.nhnacademy.command.impl;

import com.nhnacademy.command.Command;
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

@Slf4j
public class CreateRoomCommand implements Command {

    @Override
    public void execute(ClientSession session, Message request) {
        String roomName = (String) request.getPayload().getData().get("roomName");

        if (roomName == null || roomName.trim().isEmpty()) {
            return;
        }

        ChatRoom newRoom = ChatRoomManager.getInstance().createRoom(roomName);
        MessageHeader header = new MessageHeader(MessageType.CREATE_ROOM, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "ok");
        payload.getData().put("roomId", newRoom.getId());
        payload.getData().put("roomName", newRoom.getName());

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("방 생성 응답 전송 실패", e);
        }
    }
}
