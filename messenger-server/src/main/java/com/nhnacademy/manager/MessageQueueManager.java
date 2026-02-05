package com.nhnacademy.manager;

import com.nhnacademy.model.BroadcastMessage;
import com.nhnacademy.model.ChatRoom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class MessageQueueManager {
    @Getter
    private static final MessageQueueManager instance = new MessageQueueManager();
    private final BlockingQueue<BroadcastMessage> messageQueue = new LinkedBlockingQueue<>(10000);
    private final Thread workerThread;

    private MessageQueueManager() {
        workerThread = new Thread(this::processQueue);
        workerThread.setDaemon(true);
        workerThread.setName("Message-Queue-Worker");
    }

    public void start() {
        // start 메서드가 호출되면 데몬 스레드가 시작함
        if (!workerThread.isAlive()) {
            workerThread.start();
            log.info("Message Queue Worker 시작됨");
        }
    }

    public void submit(BroadcastMessage message) {
        messageQueue.offer(message);
    }

    private void processQueue() {
        // 메시지가 들어올 때까지 대기하다가 메시지가 오면 꺼내서 처리함
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

        log.debug("[Queue 처리] 방({}) 메시지 전송 시작 - 대기열 처리", room.getId());

        room.notifyObservers(message);
    }
}
