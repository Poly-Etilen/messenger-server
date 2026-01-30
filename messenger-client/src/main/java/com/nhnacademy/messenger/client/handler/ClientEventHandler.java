package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.ui.form.impl.ClientGUI;
import com.nhnacademy.util.MessageCodec;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nhnacademy.util.MessageCodec.sendMessage;

@Slf4j
public class ClientEventHandler {
    private final ClientGUI view;
    private Socket socket;
    private String myUserId;

    public ClientEventHandler(ClientGUI view) {
        this.view = view;
        connectToServer();
    }

    public void onLoginClicked(String id ,String password) {
        this.myUserId = id;

        MessageHeader header = new MessageHeader(MessageType.LOGIN, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("userId", id);
        payload.getData().put("password", password);

        sendMessage(new Message("0", header, payload));
        view.showRoomList();
    }

    public void onRoomClicked(ListView<String> roomListView) {

        view.showEnterRoom();
    }

    public void onLogoutClicked() {
        view.mainView();
    }

    public void onExitRoomClicked() {
        view.showRoomList();

    }


    private void connectToServer() {
        try {
            this.socket = new Socket("localhost", 8000);
            log.info("서버에 연결되었습니다.");

            Thread listener = new Thread(this::listen);
            listener.setDaemon(true);
            listener.start();
        } catch (IOException e) {
            log.error("서버 연결 실패", e);
        }
    }

    private void listen() {
        try {
            while (socket != null && !socket.isClosed()) {
                Message message = MessageCodec.readMessage(socket.getInputStream());
                if (message == null) {
                    break;
                }

                MessageType type = message.getHeader().getMessageType();

                Platform.runLater(() -> {
                    handleMessage(type, message);
                });
            }
        } catch (IOException e) {
            log.error("메세지 수신 중 에러", e);
        }
    }

    private void handleMessage(MessageType type, Message message) {
        Map<String, Object> data = message.getPayload().getData();
        switch (type) {
            case LOGIN_SUCCESS:
                sendRoomListRequest();
                break;
            case ROOM_LIST_RESPONSE:
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
                view.updateRoomList(rooms);
                view.showRoomList();
                break;
            case JOIN_ROOM_SUCCESS:
                // 구현 필요
            case LOGOUT_SUCCESS:
                view.mainView();
        }

    }

    private void sendRoomListRequest() {
        MessageHeader header = new MessageHeader(MessageType.ROOM_LIST,LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0",header,payload));



    }

    private  void sendMessage(Message message){
        try{
            MessageCodec.sendMessage(socket.getOutputStream(),message);
            log.debug("메세지 전송 {}",message.getHeader().getMessageType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
