package com.nhnacademy.ui.form.impl;

import com.nhnacademy.event.handler.ClientEventHandler;
import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.ui.form.View;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientGUI extends Application implements View, ClientEventListener {
    @Setter
    private String currentUser;
    private String roomName;
    private TextArea chatLog;
    private Stage primaryStage;


    private ClientEventHandler eventHandler = new ClientEventHandler(this);

    private final Map<String, String> roomNameToId = new HashMap<>();
    ObservableList<String> roomItems = FXCollections.observableArrayList();
    ListView<String> roomListView = new ListView<>(roomItems);
    ObservableList<UserItem> memberItems = FXCollections.observableArrayList();
    ListView<UserItem> memberListView = new ListView<>(memberItems);
    ObservableList<String> roomMemberItems = FXCollections.observableArrayList();
    ListView<String> roomMemberListView = new ListView<>(roomMemberItems);

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("NHN Academy Chatting Program");

        primaryStage.setOnCloseRequest(event -> {
            log.info("창이 닫혔습니다. 로그아웃 합니다");
            if (eventHandler != null) {
                eventHandler.onLogoutClicked();
                eventHandler.stopConnection();
            }
        });

        showMainView();
        primaryStage.show();
    }


    @Override
    public void showMainView() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Label label = new Label("NHN Academy Chat");
        TextField idField = new TextField();
        idField.setPromptText("아이디");
        idField.setMaxWidth(200);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("패스워드");
        passwordField.setMaxWidth(200);

        Button loginButton = new Button("로그인");
        loginButton.setMinWidth(200);

        // 이벤트는 모두 handler에게 위임
        idField.setOnAction(e ->
                eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));
        passwordField.setOnAction(e ->
                eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));
        loginButton.setOnAction(e ->
                eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));

        layout.getChildren().addAll(label, idField, passwordField, loginButton);
        primaryStage.setScene(new Scene(layout, 350, 400));
    }

    @Override
    public void showRoomListView() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        // 상단
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 0, 10, 0));
        Label userLabel = new Label("접속자: " + currentUser);
        Button createRoomBtn = new Button("방 만들기");
        Button refreshBtn = new Button("새로고침");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label label = new Label("사용자 리스트");

        refreshBtn.setOnAction(e -> eventHandler.onRefreshClicked());
        createRoomBtn.setOnAction(e -> createRoomView());

        topBar.getChildren().addAll(userLabel, createRoomBtn, refreshBtn, spacer, label);

        // 중단: 채팅방 리스트
        roomListView.setItems(roomItems);
        roomListView.setOnMouseClicked(e -> {
            String selectedRoom = roomListView.getSelectionModel().getSelectedItem();
            if (selectedRoom != null) {
                this.roomName = selectedRoom;
                eventHandler.onRoomClicked(selectedRoom);
            }
        });

        layout.setTop(topBar);
        layout.setCenter(roomListView);

        // 사용자 리스트
        memberListView.setItems(memberItems);
        memberListView.setMaxWidth(150);
        memberListView.setCellFactory(listView -> new ListCell<UserItem>() {
            @Override
            protected void updateItem(UserItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    Circle statusDot = new Circle(4);
                    statusDot.setFill(item.online ? Color.LIMEGREEN : Color.LIGHTGRAY);
                    Label label = new Label(item.id);
                    hBox.getChildren().addAll(statusDot, label);
                    setGraphic(hBox);
                    setText(null);
                }
            }
        });

        layout.setRight(memberListView);

        // 하단: 로그아웃
        Button logoutBtn = new Button("로그아웃");
        logoutBtn.setOnAction(e -> eventHandler.onLogoutClicked());
        layout.setBottom(logoutBtn);
        BorderPane.setMargin(logoutBtn, new Insets(10, 0, 0, 0));

        primaryStage.setScene(new Scene(layout, 400, 600));
    }

    @Override
    public void RoomView() {
        BorderPane layout = new BorderPane();
        roomMemberItems.clear();

        // 상단
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10, 10, 10, 10));
        Label userLabel = new Label("접속자: " + currentUser);
        Button exitButton = new Button("나가기");
        exitButton.setOnMouseClicked(e -> eventHandler.onExitRoomClicked());
        Label label = new Label("방제목: " + roomName);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        topBar.setLeft(userLabel);
        topBar.setCenter(label);
        topBar.setRight(exitButton);
        layout.setTop(topBar);

        // 채팅 로그
        chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.appendText("[System] 채팅방에 입장했습니다.\n");
        layout.setCenter(chatLog);

        // 사용자 리스트
        roomMemberListView.setItems(roomMemberItems);
        layout.setRight(roomMemberListView);

        // 메시지 입력
        TextField textField = new TextField();
        textField.setPromptText("내용을 입력해 주세요");
        layout.setBottom(textField);
        textField.setOnAction(e -> {
            String message = textField.getText().trim();
            if (message.isEmpty()) return;
            textField.clear();
            if (message.startsWith("/")) {
                eventHandler.handleCommand(message);
            } else {
                eventHandler.sendBroadCastMessage("[" + currentUser + "] " + message);
            }
        });

        Scene scene = new Scene(layout, 800, 1000);
        primaryStage.setScene(scene);
    }

    @Override
    public void createRoomView() {
        Stage createStage = new Stage();
        createStage.setTitle("CreateRoom");
        createStage.initOwner(primaryStage);
        createStage.initModality(Modality.WINDOW_MODAL);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Label label = new Label("방제목을 입력하세요");
        TextField roomNameField = new TextField();
        roomNameField.setMaxWidth(300);
        Button okBtn = new Button("확인");

        roomNameField.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.createRoomClicked(roomName);
            createStage.close();
        });
        okBtn.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.createRoomClicked(roomName);
            createStage.close();
        });

        layout.getChildren().addAll(label, roomNameField, okBtn);
        Scene scene = new Scene(layout, 300, 200);
        createStage.setScene(scene);
        createStage.show();
    }

    @Override
    public void showLogoutView() {
        showMainView();

        Stage logoutStage = new Stage();
        logoutStage.setTitle("Logout");
        logoutStage.initOwner(primaryStage);
        logoutStage.initModality(Modality.WINDOW_MODAL);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Label label = new Label("로그아웃 되셨습니다");
        Button okBtn = new Button("확인");
        okBtn.setOnAction(e -> logoutStage.close());

        layout.getChildren().addAll(label, okBtn);
        Scene scene = new Scene(layout, 300, 200);
        logoutStage.setScene(scene);
        logoutStage.show();
    }

    @Override
    public void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @Override
    public void writeMessage(String message) {
        Platform.runLater(() -> {
            if (chatLog != null) {
                chatLog.appendText(message + "\n");
            }
        });
    }

    // ============ 데이터 업데이트 메서드들 ============
    @Override
    public void updateRoomList(List<Map<String, Object>> rooms) {
        Platform.runLater(() -> {
            roomItems.clear();
            roomNameToId.clear();

            for (Map<String, Object> room : rooms) {
                String roomId = (String) room.get("roomId");
                String roomName = (String) room.get("roomName");
                roomItems.add(roomName);
                roomNameToId.put(roomName, roomId);
            }
            roomListView.setItems(roomItems);
        });
    }


    @Override
    public void updateUserList(List<Map<String, Object>> userListData) {
        if (userListData == null) return;

        Platform.runLater(() -> {
            memberItems.clear();
            for (Map<String, Object> userData : userListData) {
                Object idObj = userData.get("id");
                Object onlineObj = userData.get("online");
                if (idObj != null && onlineObj != null) {
                    String userId = (String) idObj;
                    boolean online = (boolean) onlineObj;
                    memberItems.add(new UserItem(userId, online));
                }
            }
        });
    }

    @Override
    public void updateRoomUserList(List<String> roomUserList) {
        if (roomUserList == null) return;

        Platform.runLater(() -> {
            roomMemberItems.clear();
            roomMemberItems.addAll(roomUserList);
            log.debug("이방 인원수: {}", roomUserList.size());
        });
    }

    @Override
    public void onLoginSuccess(String userId) {
        this.currentUser = userId;
    }

    @Override
    public void onLogout() {
        showLogoutView();
    }

    @Override
    public void onShowRoomList() {
        showRoomListView();

    }

    @Override
    public void onEnterRoom() {
        RoomView();
    }

    public String getRoomIdByName(String roomName) {
        return roomNameToId.get(roomName);
    }

    @AllArgsConstructor
    public static class UserItem {
        String id;
        boolean online;
    }
}