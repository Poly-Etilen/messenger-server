package com.nhnacademy.command.impl;

import com.nhnacademy.command.Command;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
public class LoginCommand implements Command {
    @Override
    public void execute(ClientSession session, Message request) {
        Map<String, Object> data = request.getPayload().getData();

        String userId = (String) data.get("userId");
        String password = (String) data.get("password");

        log.info("로그인 시도: {}", userId);

        if (authenticate(userId, password)) {
            handleSuccess(session, userId);
        } else {
            handleFail(session, userId);
        }
    }

    private void handleSuccess(ClientSession session, String userId) {
        session.setUserId(userId);
        SessionManager.getInstance().addSession(userId, session);

        MessageHeader header = new MessageHeader(MessageType.LOGIN_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "ok");
        payload.getData().put("userId", userId);

        Message response = new Message("0", header, payload);

        sendMassage(session, response);
        log.info("로그인 성공: {}", userId);
    }

    private void handleFail(ClientSession session, String userId) {
        MessageHeader header = new MessageHeader(MessageType.LOGOUT, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "fail");
        payload.getData().put("reason", "Invalid Credentials");

        Message response = new Message("0", header, payload);

        sendMassage(session, response);
        log.warn("로그인 실패: {}", userId);
    }

    private void sendMassage(ClientSession session, Message response) {
        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("전송 오류", e);
        }
    }

    private boolean authenticate(String userId, String password) {
        return "marco".equals(userId) && "nhnacademy123".equals(password);
    }
}
