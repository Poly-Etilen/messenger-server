package com.nhnacademy.command.impl;

import com.google.inject.Inject;
import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.InvalidRequestException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.CHAT_ROOM_CREATE)
public class CreateRoomCommand implements Command {

    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String roomName = PayloadExtractor.getRequired(request, MessageKey.ROOM_NAME);

        if (roomName == null || roomName.trim().isEmpty()) {
            throw new InvalidRequestException("방 이름이 공백일 수 없습니다.");
        }

        ChatRoom newRoom = chatRoomManager.createRoom(roomName);
        newRoom.addSession(session);
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_CREATE_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");
        payload.getData().put(MessageKey.ROOM_ID, newRoom.getId());
        payload.getData().put(MessageKey.ROOM_NAME, newRoom.getName());

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("방 생성 응답 전송 실패", e);
        }
    }
}
