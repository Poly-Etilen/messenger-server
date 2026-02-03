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

        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        ChatRoom room = chatRoomManager.getRoom(roomId);

        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }

        if (room.getSessions().contains(session)) {
            throw new AlreadyJoinedException();
        }

        room.addSession(session);
        sendSuccess(session, room);

        notifyEnterMember(room, session.getUserId(), roomId);
    }

    private void notifyEnterMember(ChatRoom room, String userId, String roomId) {
        MessageHeader header = new MessageHeader(MessageType.PUSH_ROOM_ENTER, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.SENDER_ID, "System");
        payload.getData().put(MessageKey.USER_NAME, userId);

        Message message = new Message("0", header, payload);

        for (ClientSession member : room.getSessions()) {
            member.sendMessage(message);
        }
    }

    private void sendSuccess(ClientSession session, ChatRoom room) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_ENTER_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");
        payload.getData().put(MessageKey.ROOM_ID, room.getId());
        payload.getData().put(MessageKey.ROOM_NAME, room.getName());

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
