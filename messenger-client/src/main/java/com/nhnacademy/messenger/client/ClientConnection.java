package com.nhnacademy.messenger.client;

import com.nhnacademy.domain.Message;
import com.nhnacademy.messenger.client.handler.ClientEventHandler;
import com.nhnacademy.util.MessageCodec;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

    private final Socket socket;
    private final ClientEventHandler handler;

    public ClientConnection(String host, int port, ClientEventHandler handler) throws IOException {
        this.socket = new Socket(host, port);
        this.handler = handler;
    }

    public void send(Message message) {
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startListening() {
        Thread listener = new Thread(this::listen);
        listener.setDaemon(true);
        listener.start();
    }

    private void listen() {
        try {
            while (!socket.isClosed()) {
                Message message = MessageCodec.readMessage(socket.getInputStream());
                if (message == null) break;

                Platform.runLater(() ->
                        handler.handleMessage(
                                message.getHeader().getMessageType(),
                                message
                        )
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
