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
import com.nhnacademy.exception.AlreadyJoinedException;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.CHAT_ROOM_ENTER)
public class JoinRoomCommand implements Command {

    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        // 요청으로부터 방 ID를 추출함
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        // 방도 가져옴
        ChatRoom room = chatRoomManager.getRoom(roomId);

        // 방어 로직
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }

        // 이미 방에 참여중인지 검증
        if (room.getSessions().contains(session.getObserver())) {
            throw new AlreadyJoinedException();
        }

        room.addSession(session.getObserver()); // 방의 참여자 목록에 자신을 추가함
        session.setCurrentRoomId(roomId);

        sendSuccess(session, room);

        // 시스템 메시지를 해당 방의 모든 사람에게 전송
        notifyEnterMember(room, session.getUserId(), roomId);
    }

    private void notifyEnterMember(ChatRoom room, String userId, String roomId) {
        MessageHeader header = new MessageHeader(MessageType.PUSH_ROOM_ENTER, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.SENDER_ID, "System");
        payload.getData().put(MessageKey.USER_NAME, userId);

        Message message = new Message(header, payload);

        for (MessageObserver member : room.getSessions()) {
            member.sendMessage(message);
        }
    }

    private void sendSuccess(ClientSession session, ChatRoom room) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_ENTER_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");
        payload.getData().put(MessageKey.ROOM_ID, room.getId());
        payload.getData().put(MessageKey.ROOM_NAME, room.getName());

        session.getObserver().sendMessage(new Message(header, payload));
    }
}
