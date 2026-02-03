package com.nhnacademy.manager;

import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.model.BroadcastMessage;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class MessageQueueManager {
    @Getter
    private static final MessageQueueManager instance = new MessageQueueManager();
    private final BlockingQueue<BroadcastMessage> messageQueue = new LinkedBlockingQueue<>();
    private final Thread workerThread;

    private MessageQueueManager() {
        workerThread = new Thread(this::processQueue);
        workerThread.setDaemon(true);
        workerThread.setName("Message-Queue-Worker");
    }

    public void start() {
        if (!workerThread.isAlive()) {
            workerThread.start();
            log.info("Message Queue Worker 시작됨");
        }
    }

    public void submit(BroadcastMessage message) {
        messageQueue.offer(message);
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                BroadcastMessage msg = messageQueue.take();
                broadcastMessage(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Queue Worker 인터럽트 발생", e);
            } catch (Exception e) {
                log.error("메시지 처리 중 오류 발생", e);
            }
        }
    }

    private void broadcastMessage(BroadcastMessage message) {
        ChatRoom room = message.getChatRoom();

        MessageHeader header = new MessageHeader(MessageType.PUSH_NEW_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, room.getId());
        payload.getData().put(MessageKey.MESSAGE_ID, message.getMessageId());
        payload.getData().put(MessageKey.SENDER_ID, message.getSenderId());
        payload.getData().put(MessageKey.CONTENT, message.getContent());
        payload.getData().put(MessageKey.TYPE, "TEXT");
        payload.getData().put(MessageKey.FILE_NAME, null);
        payload.getData().put(MessageKey.FILE_SIZE, 0);

        Message broadcastMsg = new Message("0", header, payload);

        log.debug("[Queue 처리] 방({}) 메시지 전송 시작 - 대기열 처리", room.getId());

        for (ClientSession session : room.getSessions()) {
            try {
                MessageCodec.sendMessage(session.getSocket().getOutputStream(), broadcastMsg);
            } catch (IOException e) {
                log.error("전송 실패: target={}", session.getUserId(), e);
            }
        }
    }
}
