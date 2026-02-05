package com.nhnacademy.session;

import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.model.BroadcastMessage;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.util.MessageCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class ClientMessageObserver implements MessageObserver {

    private final Socket socket;

    @Getter
    @Setter
    private String userId;

    @Override
    public void onMessage(BroadcastMessage message) {
        MessageHeader header = new MessageHeader(MessageType.PUSH_NEW_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, message.getChatRoom().getId());
        payload.getData().put(MessageKey.MESSAGE_ID, message.getMessageId());
        payload.getData().put(MessageKey.SENDER_ID, message.getSenderId());
        payload.getData().put(MessageKey.CONTENT, message.getContent());
        payload.getData().put(MessageKey.TYPE, "TEXT");
        payload.getData().put(MessageKey.FILE_NAME, null);
        payload.getData().put(MessageKey.FILE_SIZE, 0);

        sendMessage(new Message(header, payload));
    }

    public void sendMessage(Message message) {
        if (socket.isClosed()) return;
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
        } catch (IOException e) {
            log.error("메시지 전송 실패: target={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void sendErrorMessage(String message) {
        MessageHeader header = new MessageHeader(MessageType.ERROR, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.MESSAGE, message);
        sendMessage(new Message(header, payload));
    }
}
