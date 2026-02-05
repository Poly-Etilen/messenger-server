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
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.LOGOUT)
public class LogoutCommand implements Command {
    @Inject
    private ChatRoomManager chatRoomManager;
    @Inject
    private SessionManager sessionManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String userId = session.getUserId();

        String currentRoomId = session.getCurrentRoomId();
        // 현재 채팅방에 있으면 채팅방을 나감
        if (currentRoomId != null) {
            ChatRoom room = chatRoomManager.getRoom(currentRoomId);
            if (room != null) {
                room.removeSession(session.getObserver());
                log.info("채팅방 퇴장 처리: room={}, user={}", currentRoomId, userId);
            }
            session.setCurrentRoomId(null);
        }

        // 그래도 유저 ID가 있다면 세션을 제거함
        if (userId != null) {
            sessionManager.removeSession(userId);
        }

        MessageHeader header = new MessageHeader(MessageType.LOGOUT_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");

        Message message = new Message(header, payload);

        session.getObserver().sendMessage(message);
    }
}
