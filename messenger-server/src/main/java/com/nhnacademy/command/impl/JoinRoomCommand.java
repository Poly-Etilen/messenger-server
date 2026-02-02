package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.AlreadyJoinedException;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
public class JoinRoomCommand implements Command {

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        String roomId = (String) request.getPayload().getData().get("roomId");
        ChatRoom room = ChatRoomManager.getInstance().getRoom(roomId);

        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }

        if (room.getSessions().contains(session)) {
            throw new AlreadyJoinedException();
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

    private void sendMessage(ClientSession session, Message response) {
        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("응답 전송 실패", e);
        }
    }
}
