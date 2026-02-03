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
                new MessageHeader(MessageType.CHAT_ROOM_ENTER, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", selectedRoomId);

        sendMessage(new Message("0", header, payload));
    }


    public void sendBroadCastMessage(String message) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("senderId",myUserId);
        payload.getData().put("message", message);

        sendMessage(new Message("0",header,payload));


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
                roomListRequest();
                break;
            case LOGIN_FAIL:
                String reason = (String) data.get("reason");
                if (reason == null) {
                    reason = "로그인 실패";
                }
                view.showError("로그인 실패", reason);
                break;
            case CHAT_ROOM_LIST_SUCCESS:
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
                view.updateRoomList(rooms);
                break;
            case CHAT_ROOM_ENTER_SUCCESS:
                this.roomId = (String) data.get("roomId");
                this.roomName = (String) data.get("roomName");
                view.showEnterRoom();
                roomMemberListRequest();
                break;
            case CHAT_ROOM_CREATE_SUCCESS:
                this.roomId = (String) data.get("roomId");
                this.roomName = (String) data.get("roomName");
                view.showEnterRoom();
                roomListRequest();
                roomMemberListRequest();
                break;
            case CHAT_ROOM_EXIT_SUCCESS:
                roomListRequest();
                view.showRoomList();
                break;
            case CHAT_ROOM_USER_LIST_SUCCESS:
                List<Map<String, Object>> roomUserList = (List<Map<String, Object>>) data.get("userList");
                log.debug("서버 수신 유저 리스트: {}", roomUserList);
                List<String> roomUserIds = (List<String>) data.get("userList");
                view.updateMemberList(roomUserIds);
                break;
            case LOGOUT:
            case LOGOUT_SUCCESS:
                view.logout();
                break;
            case CHAT_MESSAGE:
                String senderId = (String)data.get("senderId");
                String content = (String)data.get("message");
                log.debug("메세지 수신 성공 roomId: {}, senderID: {}",roomId,senderId);
                receiveMessage(content);
                if("System".equals(senderId)){
                    roomMemberListRequest();
                }
                break;
            case CHAT_MESSAGE_SUCCESS:
                long messageId = (long) data.get("messageId");
                log.debug("메세지 전송 성공 roomId: {}, messageId: {}",roomId,messageId);
                break;

        }

    }

    private void receiveMessage(String content) {
        view.writeMessage(content);

    }

    public void leaveRoomRequest(){
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_EXIT,LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId",roomId);
        sendMessage(new Message("0",header,payload));
    }

    public void roomMemberListRequest(){
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_USER_LIST,LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId",this.roomId);
        sendMessage(new Message("0",header,payload));

    }


    public void handleCreateRoom(Stage createStage,String roomName) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_CREATE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomName",roomName);
        sendMessage(new Message("0", header, payload));
        createStage.close();



    }

    private void roomListRequest() {
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_LIST, LocalDateTime.now());
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

    public void onRefreshClicked() {
        log.info("방 목록 새로고침 요청");
        roomListRequest(); // 기존에 작성하신 private 메서드 호출
    }

    public void sendWhisperMessage(String targetId, String trim) {
    }

    public void sendChatHistoryRequest() {

    }
}
