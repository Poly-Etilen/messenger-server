package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
public class SendMessageCommand implements Command {

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        String roomId = (String) request.getPayload().getData().get("roomId");
        String messageContent = (String) request.getPayload().getData().get("message");

        ChatRoom room = ChatRoomManager.getInstance().getRoom(roomId);
        if (room == null) {
            log.warn("메시지 전송 실패: 존재하지 않는 방 (roomId={})", roomId);
            return;
        }

        String senderId = session.getUserId();
        long messageId = System.currentTimeMillis();
        room.addMessage(senderId,messageContent);

        broadcastMessage(room, senderId, messageContent);

        sendSuccessResponse(session, roomId, messageId);
    }

    private void broadcastMessage(ChatRoom room, String senderId, String Content) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", room.getId());
        payload.getData().put("senderId", senderId);
        payload.getData().put("message", Content);

        Message broadcastMsg = new Message("0", header, payload);

        for (ClientSession s : room.getSessions()) {
            try {
                MessageCodec.sendMessage(s.getSocket().getOutputStream(), broadcastMsg);
            } catch (IOException e) {
                log.error("메시지 브래드캐스트 실해: target:{}", s.getUserId(), e);
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
