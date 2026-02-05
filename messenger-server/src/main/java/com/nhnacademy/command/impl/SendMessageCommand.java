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
import com.nhnacademy.manager.MessageQueueManager;
import com.nhnacademy.model.BroadcastMessage;
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
    @Inject
    private MessageQueueManager messageQueueManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        // 요청으로부터 방 ID를 추출함
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        // 메시지 내용도 추출함
        String messageContent = PayloadExtractor.getRequired(request, MessageKey.MESSAGE);

        ChatRoom room = chatRoomManager.getRoom(roomId);
        // 방어 로직
        if (room == null) {
            log.warn("메시지 전송 실패: 존재하지 않는 방 (roomId={})", roomId);
            return;
        }

        // 참여하지 않은 유저가 메시지를 보낸 경우 차단
        if (!room.getSessions().contains(session.getObserver())) {
            log.warn("차단됨: 방에 입장하지 않는 사용자({})가 메시지 전송 시도", session.getUserId());
            return;
        }

        String senderId = session.getUserId();
        long messageId = System.currentTimeMillis();
        room.addMessage(senderId, messageContent);

        // 직접 메시지를 전송하는 방식이 아닌 큐에 넣고 끝냄. 대규모 트래픽 대비
        messageQueueManager.submit(new BroadcastMessage(room, senderId, messageContent, messageId));

        sendSuccessResponse(session, roomId, messageId);
    }

    private void sendSuccessResponse(ClientSession session, String roomId, long messageId) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("messageId", messageId);

        Message response = new Message(header, payload);

        session.getObserver().sendMessage(response);
    }
}
