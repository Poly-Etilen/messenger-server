package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
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
public class LogoutCommand implements Command {
    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String userId = session.getUserId();

        String currentRoomId = session.getCurrentRoomId();
        if (currentRoomId != null) {
            ChatRoom room = ChatRoomManager.getInstance().getRoom(currentRoomId);
            if (room != null) {
                room.removeSession(session);
                log.info("채팅방 퇴장 처리: room={}, user={}", currentRoomId, userId);
            }
            session.setCurrentRoomId(null);
        }

        if (userId != null) {
            SessionManager.getInstance().removeSession(userId);
        }

        MessageHeader header = new MessageHeader(MessageType.LOGOUT_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "ok");

        Message message = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), message);
            log.info("로그아웃 성공 처리 완료: {}", userId);
        } catch (IOException e) {
            log.error("로그아웃 응답 전송 실패", e);
        }
    }
}
