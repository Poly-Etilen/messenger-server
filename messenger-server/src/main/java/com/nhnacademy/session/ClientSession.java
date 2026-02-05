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
import com.nhnacademy.util.MessageCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Slf4j
public class ClientSession implements Runnable {
    private final Socket socket;
    private final Map<MessageType, Command> commandMap;

    private final ClientMessageObserver observer;

    @Setter
    private String currentRoomId;

    public ClientSession(Socket socket, String userId, Map<MessageType, Command> commandMap) {
        this.socket = socket;
        this.commandMap = commandMap;
        this.observer = new ClientMessageObserver(socket);
        setUserId(userId);
    }

    public void setUserId(String userId) {
        this.observer.setUserId(userId);
    }

    public String getUserId() {
        return this.observer.getUserId();
    }

    @Override
    public void run() {
        SessionHolder.set(this);
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                // 소켓에서 메시지를 읽어옴
                Message message = MessageCodec.readMessage(socket.getInputStream());

                if (message == null) break;
                // 메시지 헤더의 MessageType을 확인
                MessageType type = message.getHeader().getMessageType();
                log.debug("[{}] 수신: {}", getUserId(), type);

                //commandMap에서 해당 타입에 맞는 객체를 찾아옴
                Command command = commandMap.get(type);
                if (command != null) {
                    try {
                        // @LoginRequired가 있는지 확인함
                        checkPermission(command);
                        command.execute(message);
                    } catch (MessengerException e) {
                        // 중복 로그인, 방 없음 등 비즈니스 로직 상의 오류 분류
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
            log.info("연결 종료: {}", getUserId());
        } finally {
            SessionHolder.clear();
            disconnect();
        }

    }

    private void disconnect() {
        if (getUserId() != null) {
            SessionManager.getInstance().removeSession(getUserId());
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
            if (getUserId() == null) {
                throw new NotAuthorizedException();
            }
        }
    }

    private void handleMessengerException(MessengerException e) {
        log.warn("요청 처리 실패: {}", e.getMessage());

        // ErrorType을 사용하여 클라이언트에게 fail 실패 응답을 보냄
        MessageHeader header = new MessageHeader(e.getErrorType(), LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.RESULT, "fail");
        payload.getData().put(MessageKey.REASON, e.getMessage());

        observer.sendMessage(new Message(header, payload));
    }

    private void sendError(String message) {
        MessageHeader header = new MessageHeader(MessageType.ERROR, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.MESSAGE, message);
        sendMessage(new Message(header, payload));
    }

    public void sendMessage(Message message) {
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
        } catch (IOException e) {
            log.error("응답 전송 실패", e);
        }
    }
}
