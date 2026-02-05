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
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.util.NioMessageCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Getter
@Slf4j
public class ClientSession {
    private final SocketChannel socketChannel;
    private final Map<MessageType, Command> commandMap;
    private final MessageObserver observer;
    private final ExecutorService workerThreadPool;

    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192); // 8KB 버퍼

    @Setter
    private String currentRoomId;

    public ClientSession(SocketChannel socketChannel, Map<MessageType, Command> commandMap, ExecutorService workerThreadPool) {
        this.socketChannel = socketChannel;
        this.commandMap = commandMap;
        this.workerThreadPool = workerThreadPool;
        this.observer = new ClientMessageObserver(socketChannel);
    }

    public void setUserId(String userId) {
        this.observer.setUserId(userId);
    }

    public String getUserId() {
        return this.observer.getUserId();
    }

    public void read() throws IOException {
        int readBytes = socketChannel.read(readBuffer);
        if (readBytes == -1) {
            throw new IOException("End of stream");
        }
        if (readBytes > 0) {
            List<Message> messages = NioMessageCodec.decode(readBuffer);

            for (Message message : messages) {
                workerThreadPool.submit(() -> processMessage(message));
            }
        }
    }

    public synchronized void sendMessage(Message message) {
        if (!socketChannel.isOpen()) return;
        try {
            ByteBuffer buffer = NioMessageCodec.encode(message);
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        } catch (IOException e) {
            log.error("메시지 전송 실패", e);
            close();
        }
    }

    private void processMessage(Message message) {
        SessionHolder.set(this);
        try {
            MessageType type = message.getHeader().getMessageType();
            log.debug("[{}] 요청 수신: {}", getUserId(), type);

            Command command = commandMap.get(type);
            if (command != null) {
                checkPermission(command);
                command.execute(message);
            } else {
                log.warn("알 수 없는 명령어: {}", type);
            }
        } catch (MessengerException e) {
            handleMessengerException(e);
        } catch (Exception e) {
            log.error("명령어 처리 중 오류 발생", e);
            sendError("서버 내부 오류가 발생했습니다.");
        } finally {
            SessionHolder.clear();
        }
    }

    public void close() {
        disconnect();
    }

    private void disconnect() {
        if (getUserId() != null) {
            SessionManager.getInstance().removeSession(getUserId());
        }
        try {
            if(socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
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

        sendMessage(new Message(header, payload));
    }

    private void sendError(String message) {
        MessageHeader header = new MessageHeader(MessageType.ERROR, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.MESSAGE, message);
        sendMessage(new Message(header, payload));
    }
}
