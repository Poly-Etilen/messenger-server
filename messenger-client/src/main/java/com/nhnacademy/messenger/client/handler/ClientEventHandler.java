package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.messenger.client.command.Command;
import com.nhnacademy.messenger.client.command.CommandFactory;
import com.nhnacademy.messenger.client.command.CommandIntializer;
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
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClientEventHandler {
    private final ClientGUI view;
    private Socket socket;
    private String myUserId;
    private String roomId;
    private String senderId;
    private String receiverId;
    CommandFactory factory;
    String content;

    public ClientEventHandler(ClientGUI view) {
        this.view = view;
        factory = CommandIntializer.init(this);
        connectToServer();
    }

    //로그인 요청 전송
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


    //브로드 캐스트로 메세지 전송
    public void sendBroadCastMessage(String message) {
        MessageHeader header = new MessageHeader(MessageType.CHAT_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("senderId", myUserId);
        payload.getData().put("message", message);

        sendMessage(new Message("0", header, payload));


    }

    public void onLogoutClicked() {
        MessageHeader header = new MessageHeader(MessageType.LOGOUT, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0", header, payload));
    }

    public void onExitRoomClicked() {
        leaveRoomRequest();
    }

    public void onRefreshClicked() {
        log.info("방 목록 새로고침 요청");
        roomListRequest(); // 방리스트 동기화
        memberListRequest(); // 멤버리스트 동기화
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

    //받은 메세지 처리
    private void handleMessage(MessageType type, Message message) {
        Map<String, Object> data = message.getPayload().getData();
        switch (type) {
            case LOGIN_SUCCESS: // 로그인 성공시 방 리스트화면으로 이동, 멤버 리스트, 채팅방 리스트 불러오기
                log.info("로그인 성공");
                view.setCurrentUser(myUserId);
                view.showRoomList();
                roomListRequest();
                memberListRequest();
                break;
            case LOGIN_FAIL: // 로그인 오류시 에러창 출력
                String reason = (String) data.get("reason");
                if (reason == null) {
                    reason = "로그인 실패";
                }
                view.showError("로그인 실패", reason);
                break;
            case CHAT_ROOM_LIST_SUCCESS: // 채팅방 리스트를 불러옴
                List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
                view.updateRoomList(rooms);
                break;
            case CHAT_ROOM_ENTER_SUCCESS: // 채팅방 입장 성공시 채팅방으로 이동, 방에있는 멤버 리스트 불러오기
                this.roomId = (String) data.get("roomId");
                view.showEnterRoom();
                roomMemberListRequest();
                break;
            case CHAT_ROOM_CREATE_SUCCESS: // 채팅방 생성 성공시 채팅방으로 이동, 방에있는 멤버 리스트 불러오기
                this.roomId = (String) data.get("roomId");
                view.showEnterRoom();
                roomMemberListRequest();
                break;
            case CHAT_ROOM_EXIT_SUCCESS: // 채팅방 나가기 성공시 방 리스트 화면으로 이동후 멤버리스트, 채팅방 리스트 불러오기
                view.showRoomList();
                roomListRequest();
                memberListRequest();
                break;
            case USER_LIST_SUCCESS: // 유저 리스트를 불러옴
                List<Map<String, Object>> userListData = (List<Map<String, Object>>) data.get("userList");
                view.updateMemberList(userListData);
                for (Map<String, Object> userList : userListData) {
                    String userId = (String) userList.get("id");
                    log.debug(userId);
                }
                break;
            case CHAT_ROOM_USER_LIST_SUCCESS: // 내방에 있는 유저 리스트를 불러옴
                if (!roomId.equals((String) data.get("roomId"))) {
                    log.debug("roomId가 동일하지 않습니다");
                    break;
                }
                List<String> roomUserList = (List<String>) data.get("userList");
                log.debug("서버 수신 유저 리스트: {}", roomUserList);
                view.updateRoomMemberList(roomUserList);
                break;
            case LOGOUT:
            case LOGOUT_SUCCESS:
                view.logout();
                break;
            case CHAT_MESSAGE: // 상대방이 보낸 채팅 메세지 수신
                senderId = (String) data.get("senderId");
                content = (String) data.get("message");
                log.debug("메세지 수신 성공 roomId: {}, senderID: {}", roomId, senderId);
                receiveMessage(content);
                if ("System".equals(senderId)) {
                    roomMemberListRequest();
                }
                break;
            case CHAT_MESSAGE_SUCCESS: // 메세지 전송 성공시
                long messageId = (long) data.get("messageId");
                log.debug("메세지 전송 성공 roomId: {}, messageId: {}", roomId, messageId);
                break;
            case PRIVATE_MESSAGE_SUCCESS:
                receiverId = (String) data.get("receiverId");
                content = (String) data.get("content");
                receiveMessage("Whisper [to " + receiverId + "] : " + content);
                break;

            case PRIVATE_MESSAGE_RECEIVE:
                senderId = (String) data.get("senderId");
                content = (String) data.get("content");
                receiveMessage("Whisper [" + senderId + "] : " + content);
                break;
            case PUSH_NEW_MESSAGE:
                content = (String) data.get(MessageKey.CONTENT);
                view.writeMessage(content);
                break;
            case PUSH_ROOM_ENTER:
                String enterUser = (String) data.get(MessageKey.USER_NAME);
                if (enterUser != null) {
                    view.writeMessage("[알림] " + enterUser + " 님이 입장하셨습니다.");
                }
                roomMemberListRequest();
                break;
            case PUSH_ROOM_EXIT:
                String exitUser = (String) data.get(MessageKey.USER_ID);
                if (exitUser != null) {
                    view.writeMessage("[알림] " + exitUser + " 님이 퇴장하셨습니다.");
                }
                roomMemberListRequest();
                break;
        }

    }

    public void handleCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+", 3);
        String command = parts[0];

        switch (command) {
            case "/help":
            case "/도움말":
                view.writeMessage("""
                        [System] 명령어 목록:
                        /whisper <아이디> <메시지>
                        /history
                        /exit
                        """);
                break;
            case "/whisper":
            case "/w":
            case "/귓":
                if (parts.length < 3) {
                    view.writeMessage("[System] 사용법: /whisper <아이디> <메시지>");
                } else {
                    sendWhisperMessage(parts[1], parts[2]);
                }
                break;
            case "/history":
            case "/기록":
                sendChatHistoryRequest();
                break;
            case "/exit":
            case "/나가기":
                onExitRoomClicked();
                break;
            default:
                view.writeMessage("[System] 알 수 없는 명령어입니다: " + command);
        }
    }

    private void receiveMessage(String content) {
        view.writeMessage(content); // 채팅메세지 UI화면에 추가

    }

    public void leaveRoomRequest() { // 방나가기 요청
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_EXIT, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        sendMessage(new Message("0", header, payload));
    }

    public void memberListRequest() { // 전체 멤버리스트 조회 요청
        MessageHeader header = new MessageHeader(MessageType.USER_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0", header, payload));
    }

    public void roomMemberListRequest() { // 현재 방에있는 멤버리스트 조회 요청
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_USER_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", this.roomId);
        sendMessage(new Message("0", header, payload));

    }


    public void createRoomRequest(Stage createStage, String roomName) { //방 생성 요청
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_CREATE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomName", roomName);
        sendMessage(new Message("0", header, payload));
        createStage.close();

    }

    private void roomListRequest() { // 방리스트 조회 요청
        MessageHeader header = new MessageHeader(MessageType.CHAT_ROOM_LIST, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        sendMessage(new Message("0", header, payload));

    }

    private void sendMessage(Message message) { // 메세지를 서버로 전송
        try {
            MessageCodec.sendMessage(socket.getOutputStream(), message);
            log.debug("메세지 전송 {}", message.getHeader().getMessageType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendWhisperMessage(String targetId, String trim) {
        MessageHeader header = new MessageHeader(MessageType.PRIVATE_MESSAGE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("receiverId", targetId);
        payload.getData().put("message", trim);

        sendMessage(new Message("0", header, payload));
    }

    public void sendChatHistoryRequest() {

    }

    public void executeCommand(String message) {

        String[] arr = message.split(" ", 3);

        Command command = factory.get(arr[0]);

        if (command == null) {
            view.writeMessage("알 수 없는 명령어입니다.");
            return;
        }
        command.execute(arr);


    }


}
