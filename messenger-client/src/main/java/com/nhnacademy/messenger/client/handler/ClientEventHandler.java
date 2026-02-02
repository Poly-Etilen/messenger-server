package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.ui.form.impl.ClientGUI;
import com.nhnacademy.util.MessageCodec;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientEventHandler {
    private final ClientGUI view;
    private Socket socket;
    private String myUserId;
    private String roomId;
    private String roomName;

    public ClientEventHandler(ClientGUI view) {
        this.view = view;
        connectToServer();
    }

    public void onLoginClicked(String id, String password) {
        this.myUserId = id;

        MessageHeader header = new MessageHeader(MessageType.LOGIN, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("userId", id);
        payload.getData().put("password", password);

        sendMessage(new Message("0", header, payload));
    }

    public void onRoomClicked(String roomName) {
        String selectedRoomId = view.getRoomIdByName(roomName);

        if (selectedRoomId == null) {
            log.error("roomId를 찾을 수 없습니다. roomName={}", roomName);
            return;
        }

        MessageHeader header =
                new MessageHeader(MessageType.JOIN_ROOM, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", selectedRoomId);

        sendMessage(new Message("0", header, payload));
    }


    public void sendBroadCastMessage(String message) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", "1");
        payload.getData().put("message", message);
        //구현중

    }


    public void onLogoutClicked() {
        MessageHeader header = new MessageHeader(MessageType.LOGOUT, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0", header, payload));
    }

    public void onExitRoomClicked() {
        leaveRoomRequest();
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
                log.info("로그인 성공");
                view.setCurrentUser(myUserId);
                view.showRoomList();
                sendRoomListRequest();
                break;
            case LOGIN_FAIL:
                String reason = (String) data.get("reason");
                if (reason == null) {
                    reason = "로그인 실패";
                }
                view.showError("로그인 실패", reason);
                break;
            case ROOM_LIST_RESPONSE:
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
                view.updateRoomList(rooms);
                break;
            case JOIN_ROOM_SUCCESS:
                this.roomId = (String) data.get("roomId");
                this.roomName = (String) data.get("roomName");
                view.showEnterRoom();
                sendMemberListRequest();
                break;
            case CREATE_ROOM_SUCCESS:
                this.roomId = (String) data.get("roomId");
                this.roomName = (String) data.get("roomName");
                view.showEnterRoom();
                sendMemberListRequest();
                break;
            case LEAVE_ROOM_SUCCESS:
                sendMemberListRequest();
                sendRoomListRequest();
                view.showRoomList();
                break;
            case ROOM_USER_LIST_RESPONSE:
                roomId = (String) data.get("roomId");
                List<String> userList = (List<String>) data.get("userList");
                view.updateMemberList(userList);
                break;
            case LOGOUT:
            case LOGOUT_SUCCESS:
                view.logout();
                break;
        }

    }
    public void leaveRoomRequest(){
        MessageHeader header = new MessageHeader(MessageType.LEAVE_ROOM,LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId",roomId);
        sendMessage(new Message("0",header,payload));
    }

    public void sendMemberListRequest(){
        MessageHeader header = new MessageHeader(MessageType.ROOM_USER_LIST,LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId",this.roomId);
        sendMessage(new Message("0",header,payload));

    }
    public void handleCreateRoom(Stage createStage,String roomName) {
        MessageHeader header = new MessageHeader(MessageType.CREATE_ROOM, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomName",roomName);
        sendMessage(new Message("0", header, payload));
        createStage.close();
        sendRoomListRequest();
        view.showEnterRoom();
        this.roomId = view.getRoomIdByName(roomName);


    }

    private void sendRoomListRequest() {
        MessageHeader header = new MessageHeader(MessageType.ROOM_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0", header, payload));


    }

    private void sendMessage(Message message) {
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
            log.debug("메세지 전송 {}", message.getHeader().getMessageType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
