package com.nhnacademy.session;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.MessengerException;
import com.nhnacademy.exception.NotAuthorizedException;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.BroadcastMessage;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.util.MessageCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Slf4j
public class ClientSession implements Runnable, MessageObserver {
    private final Socket socket;

    @Setter
    private String userId;

    @Setter
    private String currentRoomId;

    private final Map<MessageType, Command> commandMap;

    public ClientSession(Socket socket, String userId, Map<MessageType, Command> commandMap) {
        this.socket = socket;
        this.userId = userId;
        this.commandMap = commandMap;
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void run() {
        SessionHolder.set(this);
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                Message message = MessageCodec.readMessage(socket.getInputStream());

                if (message == null) break;

                MessageType type = message.getHeader().getMessageType();
                log.debug("[{}] 수신: {}", userId, type);

                Command command = commandMap.get(type);
                if (command != null) {
                    try {
                        checkPermission(command);
                        command.execute(message);
                    } catch (MessengerException e) {
                        handleMessengerException(e);
                    } catch (Exception e) {
                        log.error("알 수 없는 서버 에러", e);
                        sendError("서버 내부 오류가 발생했습니다.");
                    }
                } else {
                    log.warn("알 수 없는 명령어: {}", type);
                }
            }
        } catch (IOException e) {
            log.info("연결 종료: {}", userId);
        } finally {
            SessionHolder.clear();
            disconnect();
        }

    }

    private void disconnect() {
        if (userId != null) {
            SessionManager.getInstance().removeSession(userId);
        }
        try {
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("소켓 종료 에러", e);
        }
    }

    private void checkPermission(Command command) {
        if (command.getClass().isAnnotationPresent(LoginRequired.class)) {
            if (this.userId == null) {
                throw new NotAuthorizedException();
            }
        }
    }

    private void handleMessengerException(MessengerException e) {
        log.warn("요청 처리 실패: {}", e.getMessage());

        MessageHeader header = new MessageHeader(e.getErrorType(), LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "fail");
        payload.getData().put(MessageKey.REASON, e.getMessage());

        Message response = new Message("0", header, payload);
        sendMessage(response);
    }

    private void sendError(String message) {
        MessageHeader header = new MessageHeader(MessageType.ERROR, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.MESSAGE, message);
        sendMessage(new Message("0", header, payload));
    }

    public void sendMessage(Message message) {
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
        } catch (IOException e) {
            log.error("응답 전송 실패", e);
        }
    }

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

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(this.socket.getOutputStream(), response);
        } catch (IOException e) {
            log.error("메시지 전송 실패: target={}", this.userId, e);
        }
    }
}
