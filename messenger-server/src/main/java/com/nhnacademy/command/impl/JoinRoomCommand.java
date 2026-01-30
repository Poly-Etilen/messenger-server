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
public class JoinRoomCommand implements Command {

    @Override
    public void execute(ClientSession session, Message request) {
        String roomId = (String) request.getPayload().getData().get("roomId");
        ChatRoom room = ChatRoomManager.getInstance().getRoom(roomId);

        if (room == null) {
            sendFail(session, "존재하지 않는 방입니다.");
            return;
        }

        room.addSession(session);
        sendSuccess(session, room);
    }

    private void sendSuccess(ClientSession session, ChatRoom room) {
        MessageHeader header = new MessageHeader(MessageType.JOIN_ROOM_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "ok");
        payload.getData().put("roomId", room.getId());
        payload.getData().put("roomName", room.getName());

        Message response = new Message("0", header, payload);
        sendMessage(session, response);
    }

    private void sendFail(ClientSession session, String s) {
        MessageHeader header = new MessageHeader(MessageType.JOIN_ROOM_FAIL, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "fail");
        payload.getData().put("message", s);

        Message response = new Message("0", header, payload);
        sendMessage(session, response);
    }

    private void sendMessage(ClientSession session, Message response) {
        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("응답 전송 실패", e);
        }
    }
}
