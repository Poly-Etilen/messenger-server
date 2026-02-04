package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.messenger.client.ClientConnection;
import com.nhnacademy.messenger.client.request.Request;
import com.nhnacademy.messenger.client.request.RequestFactory;
import com.nhnacademy.ui.form.impl.ClientGUI;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientEventHandler {
    private final ClientGUI view;
    private String myUserId;
    private String roomId;
    private String senderId;
    private String receiverId;
    private final ClientConnection connection;

    String content;

    public ClientEventHandler(ClientGUI view) {
        this.view = view;
        try {
            this.connection = new ClientConnection("localhost", 8000,this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connection.startListening();
    }


    private void sendRequest(Request request) {
        connection.send(request.makeMessage());
    }

    //로그인 요청 전송
    public void onLoginClicked(String id, String password) {
        this.myUserId = id;

        sendRequest(RequestFactory.loginRequest(id, password));
    }

    public void onRoomClicked(String roomName) {
        String selectedRoomId = view.getRoomIdByName(roomName);

        if (selectedRoomId == null) {
            log.error("roomId를 찾을 수 없습니다. roomName={}", roomName);
            return;
        }

        sendRequest(RequestFactory.roomJoinRequest(selectedRoomId));
    }

    public void createRoomClicked(Stage createStage, String roomName) { //방 생성 요청
        sendRequest(RequestFactory.createRoomRequest(roomName));
        createStage.close();

    }

    public void onLogoutClicked() {
        sendRequest(RequestFactory.logoutRequest());
    }

    public void onExitRoomClicked() {

        sendRequest(RequestFactory.exitRoomRequest(roomId));
    }

    public void onRefreshClicked() {
        log.info("방 목록 새로고침 요청");
        loadRoomListScene();
    }

    //귓속말 전송
    public void sendWhisperMessage(String targetId, String trim) {
        sendRequest(RequestFactory.whisperRequest(targetId,trim));
    }

    //브로드 캐스트로 메세지 전송
    public void sendBroadCastMessage(String message) {
        sendRequest(RequestFactory.broadCastRequest(roomId, myUserId, message));

    }


    //받은 메세지 처리
    public void handleMessage(MessageType type, Message message) {
        Map<String, Object> data = message.getPayload().getData();
        switch (type) {
            case LOGIN_SUCCESS: // 로그인 성공시 방 리스트화면으로 이동, 멤버 리스트, 채팅방 리스트 불러오기
                handleLoginSuccess();
                break;
            case LOGIN_FAIL: // 로그인 오류시 에러창 출력
                handleLoginFail(data);
                break;
            case CHAT_ROOM_LIST_SUCCESS: // 채팅방 리스트를 불러옴
                handleRoomList(data);
                break;
            case CHAT_ROOM_ENTER_SUCCESS: // 채팅방 입장 성공시 채팅방으로 이동, 방에있는 멤버 리스트 불러오기
                handleEnterRoom(data);
                break;
            case CHAT_ROOM_CREATE_SUCCESS: // 채팅방 생성 성공시 채팅방으로 이동, 방에있는 멤버 리스트 불러오기
                handleEnterRoom(data);
                break;
            case CHAT_ROOM_EXIT_SUCCESS: // 채팅방 나가기 성공시 방 리스트 화면으로 이동후 멤버리스트, 채팅방 리스트 불러오기
                handleExitRoom();
                break;
            case USER_LIST_SUCCESS: // 유저 리스트를 불러옴
                handleUserList(data);
                break;
            case CHAT_ROOM_USER_LIST_SUCCESS: // 내방에 있는 유저 리스트를 불러옴
                handleRoomUserList(data);
                break;
            case LOGOUT_SUCCESS:
                handleLogoutSuccess();
                break;
            case CHAT_MESSAGE: // 상대방이 보낸 채팅 메세지 수신
                handleReciveMessage(data);
                break;
            case CHAT_MESSAGE_SUCCESS: // 메세지 전송 성공시
                handleSendMessageSuccess(data);
                break;
            case PRIVATE_MESSAGE_SUCCESS:
                handleWhisperSuccess(data);
                break;
            case PRIVATE_MESSAGE_RECEIVE:
                handleWhisperRecived(data);
                break;
            case PUSH_NEW_MESSAGE:
                handlePushNewMessage(data);
                break;
            case PUSH_ROOM_ENTER:
                handlePushRoomEnter(data);
                break;
            case PUSH_ROOM_EXIT:
                handlePushRoomExit(data);
                break;
            case CHAT_MESSAGE_HISTORY_SUCCESS:
                handleHistorySuccess(data);
                break;
        }

    }

    private void handlePushRoomEnter(Map<String, Object> data) {
        String enterUser = (String) data.get(MessageKey.USER_NAME);
        if (enterUser != null) {
            view.writeMessage("[알림] " + enterUser + " 님이 입장하셨습니다.");
        }
        sendRequest(RequestFactory.roomMemberListRequest(roomId));
    }

    private void handlePushNewMessage(Map<String, Object> data) {
        content = (String) data.get(MessageKey.CONTENT);
        view.writeMessage(content);
    }

    private void handleWhisperRecived(Map<String, Object> data) {
        senderId = (String) data.get("senderId");
        content = (String) data.get("content");
        receiveMessage("Whisper [" + senderId + "] : " + content);
    }

    private void handlePushRoomExit(Map<String, Object> data) {
        String exitUser = (String) data.get(MessageKey.USER_ID);
        if (exitUser != null) {
            view.writeMessage("[알림] " + exitUser + " 님이 퇴장하셨습니다.");
        }
        sendRequest(RequestFactory.roomMemberListRequest(roomId));
    }

    private void handleHistorySuccess(Map<String, Object> data) {
        List<Map<String, String>> history = (List<Map<String, String>>) data.get("history");
        if (!roomId.equals(data.get("roomId"))) {
            log.debug("현재 방번호화 조회된 방히스토리 번호가 다름니다");
            return;
        }
        log.debug(" 채팅 히스토리 ");
        view.writeMessage("채팅 히스토리 출력");
        for (Map<String, String> chat : history) {
            String time = chat.get("timestamp");
            String senderId = chat.get("senderId");
            String content = chat.get("message");
            log.debug("{} {}: {}", time, senderId, content);
            view.writeMessage(time + " " + content);
        }
    }

    private void handleWhisperSuccess(Map<String, Object> data) {
        receiverId = (String) data.get("receiverId");
        content = (String) data.get("content");
        receiveMessage("Whisper [to " + receiverId + "] : " + content);
    }

    private void handleSendMessageSuccess(Map<String, Object> data) {
        long messageId = (long) data.get("messageId");
        log.debug("메세지 전송 성공 roomId: {}, messageId: {}", roomId, messageId);
    }

    private void handleReciveMessage(Map<String, Object> data) {
        senderId = (String) data.get("senderId");
        content = (String) data.get("message");
        log.debug("메세지 수신 성공 roomId: {}, senderID: {}", roomId, senderId);
        receiveMessage(content);
        if ("System".equals(senderId)) {
            sendRequest(RequestFactory.roomMemberListRequest(roomId));
        }
    }

    private void handleLogoutSuccess() {
        view.logout();
    }

    private void handleRoomUserList(Map<String, Object> data) {
        if (!roomId.equals((String) data.get("roomId"))) {
            log.debug("roomId가 동일하지 않습니다");
            return;
        }
        List<String> roomUserList = (List<String>) data.get("userList");
        log.debug("서버 수신 유저 리스트: {}", roomUserList);
        view.updateRoomMemberList(roomUserList);
    }

    private void handleUserList(Map<String, Object> data) {
        List<Map<String, Object>> userListData = (List<Map<String, Object>>) data.get("userList");
        view.updateMemberList(userListData);
        for (Map<String, Object> userList : userListData) {
            String userId = (String) userList.get("id");
            log.debug(userId);
        }
    }

    private void handleExitRoom() {
        view.showRoomList();
        loadRoomListScene();
    }

    private void handleEnterRoom(Map<String, Object> data) {
        this.roomId = (String) data.get("roomId");
        view.showEnterRoom();
        sendRequest(RequestFactory.roomMemberListRequest(roomId));
    }

    private void handleRoomList(Map<String, Object> data) {
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("roomList");
        view.updateRoomList(rooms);
    }

    private void handleLoginFail(Map<String, Object> data) {
        String reason = (String) data.get("reason");
        if (reason == null) {
            reason = "로그인 실패";
        }
        view.showError("로그인 실패", reason);
    }

    private void handleLoginSuccess() {
        log.info("로그인 성공");
        view.setCurrentUser(myUserId);
        handleExitRoom();
    }

    private void loadRoomListScene() { // 방목록화면 동기화
        sendRequest(RequestFactory.roomListRequest());
        sendRequest(RequestFactory.memberListRqeust());
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
                        /logout
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
                sendRequest(RequestFactory.chatHistoryRequest(roomId));
                break;
            case "/exit":
            case "/나가기":
                onExitRoomClicked();
                break;
            case "/logout":
                onExitRoomClicked();
                onLogoutClicked();
                break;
            default:
                view.writeMessage("[System] 알 수 없는 명령어입니다: " + command);
        }
    }

    private void receiveMessage(String content) {
        view.writeMessage(content); // 채팅메세지 UI화면에 추가

    }



}
