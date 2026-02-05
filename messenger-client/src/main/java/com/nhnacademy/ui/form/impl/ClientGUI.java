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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ClientGUI extends Application implements View {

    @Setter
    private String currentUser;
    private String roomName;
    TextArea chatLog;

    private Stage primaryStage;
    private ClientEventHandler eventHandler;
    private final Map<String, String> roomNameToId = new HashMap<>(); //room 이름과 id 저장한 map

    ObservableList<String> roomItems =  FXCollections.observableArrayList();
    ListView<String> roomListView =  new ListView<>(roomItems); //방목록 리스트

    ObservableList<UserItem> memberItems = FXCollections.observableArrayList();
    ListView<UserItem> memberListView = new ListView<>(memberItems); //전체 멤버 리스트

    ObservableList<String> roomMemberItems = FXCollections.observableArrayList();
    ListView<String> roomMemberListView = new ListView<>(roomMemberItems); //방 멤버 리스트


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.eventHandler = new ClientEventHandler(this);
        primaryStage.setTitle("NHN Academy Chatting Program");

        //창닫기시 로그아웃되는 기능 추가
        primaryStage.setOnCloseRequest(event -> {
            log.info("창이 닫혔습니다. 로그아웃 합니다");
            eventHandler.onLogoutClicked();
            eventHandler.stopConnection();

        });
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

        //아이디 입력칸
        TextField idField = new TextField();
        idField.setPromptText("아이디");
        idField.setMaxWidth(200);

        //패스워드 입력칸
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("패스워드");
        passwordField.setMaxWidth(200);

        //로그인 버튼
        Button loginButton = new Button("로그인");
        loginButton.setMinWidth(200);

        //엔터시 로그인 시도
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

        // 상단 : 유저 정보 , 방생성버튼, 새로고침 버튼
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 0, 10, 0));
        Label userLabel = new Label("접속자: " + currentUser);
        Button createRoomBtn = new Button("방 만들기");
        Button refreshBtn = new Button("새로고침");

        // 빈공간 여백
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label label = new Label("사용자 리스트");

        // 클릭시 방목록 새로고침
        refreshBtn.setOnAction(e -> eventHandler.onRefreshClicked());
        topBar.getChildren().addAll(userLabel, createRoomBtn,refreshBtn,spacer,label);

        // 중단 : 채팅방 리스트 (ListView 사용)
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

        // 클릭 시 방생성 창등장
        createRoomBtn.setOnAction(e -> createRoom());

        layout.setTop(topBar);
        layout.setCenter(roomListView);

        // 중단 오른쪽 : 현재 접속자 리스트 (ListView 사용)
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
                    if (item.online) {
                        statusDot.setFill(Color.LIMEGREEN);
                    } else  {
                        statusDot.setFill(Color.LIGHTGRAY);
                    }

                    Label label = new Label(item.id);
                    hBox.getChildren().addAll(statusDot, label);

                    setGraphic(hBox);
                    setText(null);
                }
            }
        });

        layout.setRight(memberListView);

        // 하단: 로그아웃 버튼
        Button logoutBtn = new Button("로그아웃");

        // 클릭시 로그아웃
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

        roomMemberItems.clear();

        // 상단 바(왼쪽 접속자명, 중앙 방제목, 오른쪽 나가기 버튼)
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10, 10, 10, 10));

        Label userLabel = new Label("접속자: " + currentUser);

        Button exitButton = new Button("나가기");

        // 클릭시 방 나가기
        exitButton.setOnMouseClicked(e -> eventHandler.onExitRoomClicked());

        Label label = new Label("방제목: " + roomName);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        topBar.setLeft(userLabel);
        topBar.setCenter(label);
        topBar.setRight(exitButton);


        layout.setTop(topBar);

        // 채팅 메세지창
        chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.appendText("[System] 채팅방에 입장했습니다.\n");
        layout.setCenter(chatLog);

        // 사용자 리스트
        roomMemberListView.setItems(roomMemberItems);
        layout.setRight(roomMemberListView);

        // 메세지 입력창
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

    public void writeMessage(String message) {
        chatLog.appendText(message + "\n");
    }


    //로그아웃창
    @Override
    public void logout() {
        mainView();

        // 로그아웃 새창 생성
        Stage logoutStage = new Stage();
        logoutStage.setTitle("Logout");

        // 로그아웃 확인창 닫을때까지 대기
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

    public void updateRoomMemberList(List<String> memberList) {
        if (Objects.isNull(memberList)) {
            return;
        }

        Platform.runLater(() -> {
            roomMemberItems.clear();
            roomMemberItems.addAll(memberList);
            log.debug("이방 인원수 {}: ",memberList.size());
        });

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
        roomNameField.setMaxWidth(300);

        Button okBtn = new Button("확인");

        //방제목 입력후 엔터나 확인버튼 클릭시 방생성
        roomNameField.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.createRoomClicked(logoutStage, roomName);
        });
        okBtn.setOnAction(e -> {
            this.roomName = roomNameField.getText();
            eventHandler.createRoomClicked(logoutStage, roomName);
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


    public void updateMemberList(List<Map<String, Object>> userListData) {
        if (userListData == null) {
            return;
        }

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

    @AllArgsConstructor
    public static class UserItem {
        String id;
        boolean online;
    }
}