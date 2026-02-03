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
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.CHAT_ROOM_EXIT)
public class LeaveRoomCommand implements Command {

    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        if (roomId == null) {
            roomId = session.getCurrentRoomId();
        }

        if (roomId == null) {
            log.error("방 나가기 실패: 참여 중인 방이 없습니다.");
            return;
        }

        ChatRoom room = chatRoomManager.getRoom(roomId);
        if (room != null) {
            room.removeSession(session);
            notifyLeaveMember(room, session.getUserId(), roomId);
        }
        session.setCurrentRoomId(null);

        sendSuccessResponse(session);
        log.info("방 나가기 완료: user={}, room={}", session.getUserId(), roomId);
    }

    private void notifyLeaveMember(ChatRoom room, String userId, String roomId) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.SENDER_ID, "System");
        payload.getData().put(MessageKey.MESSAGE, userId + " 님이 퇴장하셨습니다.");

        Message message = new Message("0", header, payload);

        for (ClientSession member : room.getSessions()) {
            member.sendMessage(message);
        }
    }

    private void sendSuccessResponse(ClientSession session) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_EXIT_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");

        session.sendMessage(new Message("0", header, payload));
    }
}
