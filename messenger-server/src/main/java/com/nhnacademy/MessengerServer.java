package com.nhnacademy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.nhnacademy.command.Command;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.manager.MessageQueueManager;
import com.nhnacademy.module.MessengerModule;
import com.nhnacademy.session.ClientSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class MessengerServer {
    private final int port;
    private final Map<MessageType, Command> commandMap;

    public void start() {
        try (ServerSocket socket = new ServerSocket(port)){
            log.info("메신저 서버가 포트 {}에서 시작되었습니다.", port);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = socket.accept();
                    log.info("새로운 클라이언트 접속: {}", clientSocket.getInetAddress());
                    ClientSession session = new ClientSession(clientSocket, null, commandMap);
                    Thread sessionThread = new Thread(session);
                    sessionThread.start();
                } catch (IOException e) {
                    log.error("클라이언트 연결 수락 중 오류 발생", e);
                }
            }
        } catch (IOException e) {
            log.error("서버 시작 실패: 포트 {}를 사용할 수 없습니다.", port, e);
        }
    }

    public static void main(String[] args) {
        int port = 8000;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("잘못된 포트 번호입니다. 기본값({})을 사용합니다.", port);
            }
        }

        Injector injector = Guice.createInjector(new MessengerModule());
        MessageQueueManager queueManager = injector.getInstance(MessageQueueManager.class);
        queueManager.start();

        Map<MessageType, Command> commandMap = injector.getInstance(
                Key.get(new TypeLiteral<Map<MessageType, Command>>() {})
        );

        MessengerServer server = new MessengerServer(port, commandMap);
        server.start();
    }
}