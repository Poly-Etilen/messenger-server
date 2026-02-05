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
import com.nhnacademy.exception.UserNotFoundException;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.PRIVATE_MESSAGE)
public class WhisperMessageCommand implements Command {

    @Inject
    private SessionManager sessionManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        // 내가 다른 클라이언트에게 보내기 때문에 senderId는 현재 userId임
        String senderId = session.getUserId();
        // 요청으로부터 수신자 ID와 메시지 내용을 추출함
        String receiverId = PayloadExtractor.getRequired(request, MessageKey.RECEIVER_ID);
        String messageContent = PayloadExtractor.getRequired(request, MessageKey.MESSAGE);

        // 상대방의 세션 객체를 직접 찾음
        ClientSession receiverSession = sessionManager.getSession(receiverId);

        // 상대방이 오프라인이라면 보낼 수 없음
        if (receiverSession == null) {
            throw new UserNotFoundException(receiverId);
        }

        sendToReceiver(receiverSession, senderId, messageContent);

        sendToSender(session, receiverId, messageContent);
    }

    // 귓속말 전송
    private void sendToReceiver(ClientSession receiver, String senderId, String content) {
        MessageHeader header = new MessageHeader(MessageType.PRIVATE_MESSAGE_RECEIVE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.SENDER_ID, senderId);
        payload.getData().put("content", content);

        receiver.getObserver().sendMessage(new Message(header, payload));
    }

    // 귓속말을 보내는 내용은 송신자도 보여야 함
    private void sendToSender(ClientSession sender, String receiverId, String messageContent) {
        MessageHeader header = new MessageHeader(MessageType.PRIVATE_MESSAGE_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RECEIVER_ID, receiverId);
        payload.getData().put("content", messageContent);

        sender.getObserver().sendMessage(new Message(header, payload));
    }
}
