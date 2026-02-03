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
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.CHAT_MESSAGE)
public class SendMessageCommand implements Command {

    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        String messageContent = PayloadExtractor.getRequired(request, MessageKey.MESSAGE);

        ChatRoom room = chatRoomManager.getRoom(roomId);
        if (room == null) {
            log.warn("메시지 전송 실패: 존재하지 않는 방 (roomId={})", roomId);
            return;
        }

        if (!room.getSessions().contains(session)) {
            log.warn("차단됨: 방에 입장하지 않는 사용자({})가 메시지 전송 시도", session.getUserId());
            return;
        }

        String senderId = session.getUserId();
        long messageId = System.currentTimeMillis();
        room.addMessage(senderId,messageContent);

        broadcastMessage(room, senderId, messageContent, messageId);

        sendSuccessResponse(session, roomId, messageId);
    }

    private void broadcastMessage(ChatRoom room, String senderId, String Content, long messageId) {
        MessageHeader header = new MessageHeader(MessageType.PUSH_NEW_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, room.getId());
        payload.getData().put(MessageKey.MESSAGE_ID, messageId);
        payload.getData().put(MessageKey.SENDER_ID, senderId);
        payload.getData().put(MessageKey.CONTENT, Content);
        payload.getData().put(MessageKey.TYPE, "TEXT");
        payload.getData().put(MessageKey.FILE_NAME, null);
        payload.getData().put(MessageKey.FILE_SIZE, 0);

        Message broadcastMsg = new Message("0", header, payload);

        log.debug("브로드캐스트 [PUSH_NEW_MESSAGE]: 방({}), 발신자({})", room.getId(), senderId);

        for (ClientSession s : room.getSessions()) {
            try {
                MessageCodec.sendMessage(s.getSocket().getOutputStream(), broadcastMsg);
            } catch (IOException e) {
                log.error("메시지 브로드캐스트 실행: target:{}", s.getUserId(), e);
            }
        }
    }

    private void sendSuccessResponse(ClientSession session, String roomId, long messageId) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("messageId", messageId);

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("메시지 전송 성공 응답 실패", e);
        }
    }
}
