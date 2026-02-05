package com.nhnacademy.messenger.client;

import com.nhnacademy.domain.Message;
import com.nhnacademy.messenger.client.handler.ClientEventHandler;
import com.nhnacademy.util.MessageCodec;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class ClientConnection {

    private final Socket socket;
    private final ClientEventHandler handler;
    private final BlockingQueue<Message> receiveQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    Thread listenThread;
    Thread sendThread;
    Thread processThread;

    public ClientConnection(String host, int port, ClientEventHandler handler) throws IOException {
        this.socket = new Socket(host, port);
        this.handler = handler;

        startListening();
        startSending();
        startProcessing();
    }

    public void send(Message message){
        sendQueue.offer(message);
    }

    public void startListening() {
        listenThread = new Thread(() -> {
            listenThread.setName("ListenThread");
            try {
                log.debug(Thread.currentThread().getName() + " 실행 시작");
                while (!socket.isClosed() && running) {
                    Message message = MessageCodec.readMessage(socket.getInputStream());
                    if(message != null){
                        receiveQueue.put(message);
                    }
                }
            }
            catch (InterruptedException  | IOException e){
                log.error(Thread.currentThread().getName()+ " interrupt");
                Thread.currentThread().interrupt();
            }
        });
        listenThread.start();
    }

    public void startSending(){
        sendThread = new Thread(() -> {
            sendThread.setName("SendThread");
            log.debug(Thread.currentThread().getName() + " 실행 시작");
            while (!socket.isClosed() && running){
                try {
                    Message message = sendQueue.take();
                    MessageCodec.sendMessage(socket.getOutputStream(), message);
                } catch (InterruptedException  | IOException e) {
                    log.error(Thread.currentThread().getName() + " interrupt");
                    Thread.currentThread().interrupt();
                }
            }
        });
        sendThread.start();
    }

    public void startProcessing(){
        processThread = new Thread(() -> {
            processThread.setName("ProcessThread");
            log.debug(Thread.currentThread().getName() + " 실행 시작");
            while (!socket.isClosed() && running){
                try {
                    Message message = receiveQueue.take();
                    Platform.runLater(() ->
                            handler.handleMessage(message.getHeader().getMessageType(), message)
                    );
                } catch (InterruptedException e) {
                    log.error(Thread.currentThread().getName() + " interrupt");
                    Thread.currentThread().interrupt();
                }
            }
        });
        processThread.start();
    }

    // 창닫기시 정지
    public void stop() {
        running = false;

        listenThread.interrupt();
        sendThread.interrupt();
        processThread.interrupt();

        try {
            //스레드 종료 대기
            listenThread.join();
            sendThread.join();
            processThread.join();

            socket.close();
        } catch (IOException |InterruptedException e) {
            log.error("종료 중 에러 발생",e);
        }
    }
}