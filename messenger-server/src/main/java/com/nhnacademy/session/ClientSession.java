package com.nhnacademy.session;

import com.nhnacademy.command.Command;
import com.nhnacademy.command.impl.LoginCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.util.MessageCodec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class ClientSession implements Runnable{
    private final Socket socket;

    @Setter
    private String userId;

    private final Map<MessageType, Command> commandMap = new HashMap<>();

    public  ClientSession(Socket socket) {
        this.socket = socket;
        initializeCommands();
    }

    private void initializeCommands() {
        commandMap.put(MessageType.LOGIN, new LoginCommand());
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected() && !socket.isClosed()) {
                Message message = MessageCodec.readMessage(socket.getInputStream());

                if (message == null) break;

                MessageType type = message.getHeader().getMessageType();
                log.debug("[{}] 수신: {}", userId, type);

                Command command = commandMap.get(type);
                if (command != null) {
                    command.execute(this, message);
                } else {
                    log.warn("알 수 없는 명령어: {}", type);
                }
            }
        } catch (IOException e) {
            log.info("연결 종료: {}", userId);
        } finally {
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
}
