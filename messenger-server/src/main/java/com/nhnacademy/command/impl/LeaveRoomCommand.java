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
import com.nhnacademy.observer.MessageObserver;
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

        // 요청에서 방 ID를 추출함
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        // 나갈 방이 없을 경우 현재 있는 방을 기준으로함.
        if (roomId == null) {
            roomId = session.getCurrentRoomId();
        }

        // 그래도 방이 없다면 에러 발생
        if (roomId == null) {
            log.error("방 나가기 실패: 참여 중인 방이 없습니다.");
            return;
        }

        // 현재 참여중인 채팅방 요청의 방ID를 통해 방을 가져옴
        ChatRoom room = chatRoomManager.getRoom(roomId);
        if (room != null) {
            room.removeSession(session.getObserver());
            notifyLeaveMember(room, session.getUserId(), roomId); // 방에 있는 모든 클라이언트에게 퇴장 메시지를 보냄
        }
        session.setCurrentRoomId(null); // 현제 세션을 null로 설정함

        sendSuccessResponse(session);
        log.info("방 나가기 완료: user={}, room={}", session.getUserId(), roomId);
    }

    private void notifyLeaveMember(ChatRoom room, String userId, String roomId) {
        MessageHeader header = new MessageHeader(MessageType.PUSH_ROOM_EXIT, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.USER_ID, userId);

        Message message = new Message(header, payload);

        for (MessageObserver member : room.getSessions()) {
            member.sendMessage(message);
        }
    }

    private void sendSuccessResponse(ClientSession session) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_EXIT_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "ok");

        session.sendMessage(new Message(header, payload));
    }
}
