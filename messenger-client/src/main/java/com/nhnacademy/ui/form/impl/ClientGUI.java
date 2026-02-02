package com.nhnacademy.ui.form.impl;

import com.nhnacademy.messenger.client.handler.ClientEventHandler;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientGUI extends Application implements View {

    @Setter
    private String currentUser;
    private String roomName;

    private Stage primaryStage;
    private ClientEventHandler eventHandler;
    private final Map<String, String> roomNameToId = new HashMap<>();
    private List<String> memberList;
    ListView<String> roomListView;
    ListView<String> memberListView;

    ObservableList<String> roomItems;
    ObservableList<String> memberItems;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.eventHandler = new ClientEventHandler(this);
        primaryStage.setTitle("NHN Academy Chatting Program");
        mainView();
        primaryStage.show();
    }

    // 로그인 화면
    @Override
    public void mainView() {
        VBox layout = new VBox(15); //
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

        idField.setOnAction(e -> eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));
        passwordField.setOnAction(e -> eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));
        loginButton.setOnAction(e -> eventHandler.onLoginClicked(idField.getText(), passwordField.getText()));

        layout.getChildren().addAll(label, idField, passwordField, loginButton);
        primaryStage.setScene(new Scene(layout, 350, 400));
    }

    // 채팅방 목록 화면
    @Override
    public void showRoomList() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        // 상단 : 유저 정보 및 방 생성 버튼
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 0, 10, 0));
        Label userLabel = new Label("접속자: " + currentUser);
        Button createRoomBtn = new Button("방 만들기");
        topBar.getChildren().addAll(userLabel, createRoomBtn);

        // 중단 : 채팅방 리스트 (ListView 사용)
        roomListView = new ListView<>();
        roomItems = FXCollections.observableArrayList();
        roomListView.setItems(roomItems);


        // 클릭 시 방에 입장
        roomListView.setOnMouseClicked(e -> {
            String selectedRoom =
                    roomListView.getSelectionModel().getSelectedItem();
            if (selectedRoom != null) {
                this.roomName = selectedRoom;
                eventHandler.onRoomClicked(selectedRoom);
            }
        });

        //클릭 시 방생성 창등장
        createRoomBtn.setOnAction(e -> createRoom());

        layout.setTop(topBar);
        layout.setCenter(roomListView);

        // 하단: 나가기 버튼(로그인 화면으로 복귀)
        Button logoutBtn = new Button("로그아웃");
        logoutBtn.setOnAction(e -> {
            logout();
            eventHandler.onLogoutClicked();
        });
        layout.setBottom(logoutBtn);
        BorderPane.setMargin(logoutBtn, new Insets(10, 0, 0, 0));

        primaryStage.setScene(new Scene(layout, 400, 600));
    }

    // 채팅방 내부 화면
    @Override
    public void showEnterRoom() {
        BorderPane layout = new BorderPane();

        // 상단 바(왼쪽 접속자명, 중앙 방제목, 오른쪽 나가기 버튼)
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10, 10, 10, 10));

        Label userLabel = new Label("접속자: " + currentUser);

        Button exitButton = new Button("나가기");

        //나가기 버튼(채팅방 목록으로 돌아가기)
        exitButton.setOnMouseClicked(e -> eventHandler.onExitRoomClicked());


        Label label = new Label("방제목: " + roomName);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        topBar.setLeft(userLabel);
        topBar.setCenter(label);
        topBar.setRight(exitButton);


        layout.setTop(topBar);

        TextArea chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.appendText("[System] 채팅방에 입장했습니다.\n");
        layout.setCenter(chatLog);

        //사용자 리스트
        memberListView = new ListView<>();
        memberItems = FXCollections.observableArrayList();
        memberListView.setItems(memberItems);

        layout.setRight(memberListView);


        TextField textField = new TextField();
        textField.setPromptText("내용을 입력해 주세요");
        layout.setBottom(textField);
        textField.setOnAction(e -> {
            String message = textField.getText();
            textField.clear();
            chatLog.appendText(message + "\n");
            String sendMessage = currentUser + " : " + message;
            eventHandler.sendBroadCastMessage(sendMessage);

        });


        Scene scene = new Scene(layout, 800, 1000);
        primaryStage.setScene(scene);
    }

    @Override
    public void logout() {
        mainView();

        //로그아웃 새창 생성
        Stage logoutStage = new Stage();
        logoutStage.setTitle("Logout");

        //로그아웃 확인창 닫을때까지 대기
        logoutStage.initOwner(primaryStage);
        logoutStage.initModality(Modality.WINDOW_MODAL);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        Label label = new Label("로그아웃 되셧습니다");
        Button okBtn = new Button("확인");

        okBtn.setOnAction(e -> logoutStage.close());

        layout.getChildren().addAll(label, okBtn);

        Scene scene = new Scene(layout, 300, 200);
        logoutStage.setScene(scene);
        logoutStage.show();


    }

    @Override
    public void updateRoomList(List<Map<String, Object>> rooms) {

        roomItems.clear();
        roomNameToId.clear();

        for (Map<String, Object> room : rooms) {
            String roomId = (String) room.get("roomId");
            String roomName = (String) room.get("roomName");

            roomItems.add(roomName);
            roomNameToId.put(roomName, roomId);
        }

        roomListView.setItems(roomItems);

    }

    public void updateMemberList(List<String> memberList, String roomId) {

        if (!roomId.equals(getRoomIdByName(roomName))) {
            log.debug("동기화 오류");

        }
        memberItems.addAll(memberList);
        memberListView.setItems(memberItems);

    }


    @Override
    public void createRoom() {
        //방생성 새창 생성
        Stage logoutStage = new Stage();
        logoutStage.setTitle("CreateRoom");

        //방생성 창 닫을때까지 대기
        logoutStage.initOwner(primaryStage);
        logoutStage.initModality(Modality.WINDOW_MODAL);

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);


        Label label = new Label("방제목을 입력하세요");
        TextField roomNameField = new TextField();
        roomNameField.setMaxWidth(200);

        Button okBtn = new Button("확인");

        //방제목 입력후 엔터나 확인버튼 클릭시 방생성
        roomNameField.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.handleCreateRoom(logoutStage, roomName);
        });
        okBtn.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.handleCreateRoom(logoutStage, roomName);
        });

        layout.getChildren().addAll(label, roomNameField, okBtn);

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

    public String getRoomIdByName(String roomName) {
        return roomNameToId.get(roomName);
    }


}