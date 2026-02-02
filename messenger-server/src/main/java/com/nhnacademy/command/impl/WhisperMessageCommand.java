package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.UserNotFoundException;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@LoginRequired
public class WhisperMessageCommand implements Command {

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String senderId = session.getUserId();
        String receiverId = (String) request.getPayload().getData().get("receiverId");
        String messageContent = (String) request.getPayload().getData().get("message");

        ClientSession receiverSession = SessionManager.getInstance().getSession(receiverId);

        if (receiverSession == null) {
            throw new UserNotFoundException(receiverId);
        }

        sendToReceiver(receiverSession, senderId, messageContent);

        sendToSender(session, receiverId, messageContent);
    }

    private void sendToReceiver(ClientSession receiver, String senderId, String content) {
        MessageHeader header = new MessageHeader(MessageType.WHISPER_MESSAGE_RECEIVE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("receiverId", senderId);
        payload.getData().put("content", content);

        sendMessage(receiver, new Message("0", header, payload));
    }

    private void sendToSender(ClientSession sender, String receiverId, String messageContent) {
        MessageHeader header = new MessageHeader(MessageType.WHISPER_MESSAGE_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("receiverId", receiverId);
        payload.getData().put("content", messageContent);

        sendMessage(sender, new Message("0", header, payload));
    }

    private void sendMessage(ClientSession session, Message message) {
        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), message);
        } catch (IOException e) {
            log.error("메시지 전송 실패", e);
        }
    }
}
