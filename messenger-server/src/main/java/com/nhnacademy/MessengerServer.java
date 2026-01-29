package com.nhnacademy;

import com.nhnacademy.session.ClientSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class MessengerServer {
    private final int port;

    public void start() {
        try (ServerSocket socket = new ServerSocket(port)){
            log.info("매신저 서버가 포트 {}에서 시작되었습니다.", port);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = socket.accept();
                    log.info("새로운 클라이언트 접속: {}", clientSocket.getInetAddress());
                    ClientSession session = new ClientSession(clientSocket);
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
        MessengerServer server = new MessengerServer(port);
        server.start();
    }
}