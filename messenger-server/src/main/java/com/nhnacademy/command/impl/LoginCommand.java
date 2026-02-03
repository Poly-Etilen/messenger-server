package com.nhnacademy.command.impl;

import com.google.inject.Inject;
import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.DuplicateLoginException;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.repository.UserRepository;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@CommandMapping(MessageType.LOGIN)
public class LoginCommand implements Command {

    @Inject
    private SessionManager sessionManager;
    @Inject
    private UserRepository userRepository;

    private static final Map<String, String> userDatabase = new HashMap<>();

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        Map<String, Object> data = request.getPayload().getData();

        String userId = PayloadExtractor.getRequired(request, MessageKey.USER_ID);
        String password = PayloadExtractor.getRequired(request, MessageKey.PASSWORD);

        log.info("로그인 시도: {}", userId);

        if (sessionManager.isLoggedIn(userId)) {
            throw new DuplicateLoginException(userId);
        }

        if (userRepository.authenticate(userId, password)) {
            handleSuccess(session, userId);
        } else {
            handleFail(session, userId);
        }
    }

    private void handleSuccess(ClientSession session, String userId) {
        session.setUserId(userId);
        sessionManager.addSession(userId, session);

        MessageHeader header = new MessageHeader(MessageType.LOGIN_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "ok");
        payload.getData().put("userId", userId);

        Message response = new Message("0", header, payload);

        sendMassage(session, response);
        log.info("로그인 성공: {}", userId);
    }

    private void handleFail(ClientSession session, String userId) {
        MessageHeader header = new MessageHeader(MessageType.LOGIN_FAIL, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("result", "fail");
        payload.getData().put("reason", "아이디 또는 비밀번호가 틀렸습니다.");

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
}
