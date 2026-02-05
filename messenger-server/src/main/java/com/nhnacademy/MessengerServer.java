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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MessengerServer {
    private final int port;
    private final Map<MessageType, Command> commandMap;
    private final ExecutorService workerThreadPool;
    private Selector selector;

    public MessengerServer(int port, Map<MessageType, Command> commandMap) {
        this.port = port;
        this.commandMap = commandMap;
        this.workerThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public void start() {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("메신저 서버가 포트 {}에서 시작되었습니다.", port);

            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            log.error("서버 실행 중 오류 발생", e);
        } finally {
            stop();
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);

            log.info("새로운 클라이언트 접속: {}", clientChannel.getRemoteAddress());

            ClientSession session = new ClientSession(clientChannel, commandMap, workerThreadPool);
            clientChannel.register(selector, SelectionKey.OP_READ, session);
        } catch (IOException e) {
            log.error("클라이언트 연결 수락 실패", e);
        }
    }

    private void handleRead(SelectionKey key) {
        ClientSession session = (ClientSession) key.attachment();
        try {
            session.read();
        } catch (IOException e) {
            log.info("클라이언트 연결 종료 감지: {}", session.getUserId());
            session.close();
            key.cancel();
        }
    }

    private void stop() {
        if (workerThreadPool != null) {
            workerThreadPool.shutdown();
        }
        try {
            if (selector != null) selector.close();
        } catch (IOException e) {
            log.error("Selector 종료 오류", e);
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

        // 의존성을 주입받음
        Injector injector = Guice.createInjector(new MessengerModule());
        // MessageQueue를 시작시킴
        MessageQueueManager queueManager = injector.getInstance(MessageQueueManager.class);
        queueManager.start();

        Map<MessageType, Command> commandMap = injector.getInstance(
                Key.get(new TypeLiteral<Map<MessageType, Command>>() {})
        );

        MessengerServer server = new MessengerServer(port, commandMap);
        server.start();
    }
}