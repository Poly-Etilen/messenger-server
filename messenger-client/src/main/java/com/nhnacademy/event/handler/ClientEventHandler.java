package com.nhnacademy.event.handler;

import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.messenger.client.ClientConnection;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.observer.impl.AuthObserver;
import com.nhnacademy.observer.impl.ChatObserver;
import com.nhnacademy.observer.impl.RoomObserver;
import com.nhnacademy.request.Request;
import com.nhnacademy.request.RequestFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientEventHandler {
    private final List<MessageObserver> observers = new ArrayList<>();
    private final ClientConnection connection;
    private final ClientEventListener listener;
    private final CommandHandler commandHandler;

    @Getter @Setter private String myUserId;
    @Getter @Setter private String roomId;

    public ClientEventHandler(ClientEventListener listener) {
        this.listener = listener;
        this.commandHandler = new CommandHandler(listener, this);

        try {
            this.connection = new ClientConnection("localhost", 8000, this);
        } catch (IOException e) {
            log.error("서버 연결 실패", e);
            listener.showError("연결 실패", "서버가 실행 중인지 확인해주세요.");
            throw new RuntimeException(e);
        }

        // 옵저버 등록
        addObserver(new AuthObserver(listener, this));
        addObserver(new RoomObserver(listener, this));
        addObserver(new ChatObserver(listener, this));
    }

    public void addObserver(MessageObserver observer) {
        observers.add(observer);
    }

    // 서버 메시지 수신 시 호출됨
    public void handleMessage(MessageType type, Message message) {
        Map<String, Object> data = message.getPayload().getData();
        log.debug("메시지 수신: type={}, data={}", type, data);

        // 모든 옵저버에게 알림
        for (MessageObserver observer : observers) {
            observer.execute(type, data);
        }
    }

    // 공통 요청 전송 메서드
    public void sendRequest(Request request) {
        connection.send(request.makeMessage());
    }

    public void stopConnection() {
        if (connection != null) {
            log.info("서버 연결 종료 중...");
            connection.stop();
        }
    }

    // --- UI 이벤트 연동 메서드들 ---
    public void onLoginClicked(String id, String password) {
        this.myUserId = id;
        sendRequest(RequestFactory.loginRequest(id, password));
    }

    public void onRoomClicked(String roomName) {
        String selectedRoomId = listener.getRoomIdByName(roomName);
        if (selectedRoomId != null) sendRequest(RequestFactory.roomJoinRequest(selectedRoomId));
    }

    public void createRoomClicked(String roomName) {
        sendRequest(RequestFactory.createRoomRequest(roomName));
    }

    public void onLogoutClicked() { sendRequest(RequestFactory.logoutRequest()); }

    public void onExitRoomClicked() { sendRequest(RequestFactory.exitRoomRequest(roomId)); }

    public void onRefreshClicked() { loadRoomListScene(); }

    public void handleCommand(String commandLine) { commandHandler.handler(commandLine); }

    public void sendBroadCastMessage(String message) {
        sendRequest(RequestFactory.broadCastRequest(roomId, myUserId, message));
    }

    public void loadRoomListScene() {
        sendRequest(RequestFactory.roomListRequest());
        sendRequest(RequestFactory.memberListRequest());
    }
}