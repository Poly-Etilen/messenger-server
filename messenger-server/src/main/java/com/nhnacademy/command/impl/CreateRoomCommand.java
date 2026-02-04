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
        // 요청에서 방 이름을 추출함
        String roomName = PayloadExtractor.getRequired(request, MessageKey.ROOM_NAME);

        // 방어 로직
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new InvalidRequestException("방 이름이 공백일 수 없습니다.");
        }

        ChatRoom newRoom = chatRoomManager.createRoom(roomName); // 매니저를 호출해서 UUID를 생성하고 ChatRoom을 만들고 저장소에 등록함
        newRoom.addSession(session);
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_CREATE_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");
        payload.getData().put(MessageKey.ROOM_ID, newRoom.getId());
        payload.getData().put(MessageKey.ROOM_NAME, newRoom.getName());

        Message response = new Message(header, payload);

        try {
            // 성공 시 방의 ID를 포함하여 성공 응답을 보냄
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("방 생성 응답 전송 실패", e);
        }
    }
}
