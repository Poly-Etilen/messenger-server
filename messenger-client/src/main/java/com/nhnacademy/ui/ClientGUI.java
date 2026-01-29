package com.nhnacademy.ui;

import com.nhnacademy.messenger.client.handler.ClientEventHandler;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class ClientGUI extends Application implements View {

    private Stage primaryStage;
    private ClientEventHandler eventHandler;

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


        loginButton.setOnAction(e -> eventHandler.onLoginClicked(idField.getText(),passwordField.getText()));

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
        Label userLabel = new Label("접속자: test1234");
        Button createRoomBtn = new Button("방 만들기");
        topBar.getChildren().addAll(userLabel, createRoomBtn);

        // 중단 : 채팅방 리스트 (ListView 사용)
        ListView<String> roomListView = new ListView<>();
        ObservableList<String> rooms = FXCollections.observableArrayList(
                "General",
                "개발자 모임",
                "자유 게시판"
        );
        roomListView.setItems(rooms);

        // 클릭 시 방에 입장
        roomListView.setOnMouseClicked(e -> eventHandler.onRoomClicked(roomListView));

        layout.setTop(topBar);
        layout.setCenter(roomListView);

        // 하단: 나가기 버튼(로그인 화면으로 복귀)
        Button logoutBtn = new Button("로그아웃");
        logoutBtn.setOnAction(e -> mainView());
        layout.setBottom(logoutBtn);
        BorderPane.setMargin(logoutBtn, new Insets(10, 0, 0, 0));

        primaryStage.setScene(new Scene(layout, 400, 600));
    }

    // 채팅방 내부 화면
    public void showEnterRoom() {
        BorderPane layout = new BorderPane();

        // 상단 바
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(10, 10, 10, 10));

        Label userLabel = new Label("접속자: test1234");

        Label roomName = new Label("방제목: General");
        roomName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");


        topBar.setLeft(userLabel);
        topBar.setCenter(roomName);
        BorderPane.setAlignment(roomName, Pos.TOP_RIGHT);

        layout.setTop(topBar);

        TextArea chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.appendText("[System] 채팅방에 입장했습니다.\n");
        layout.setCenter(chatLog);

        TextField textField = new TextField();
        textField.setPromptText("내용을 입력해 주세요");
        layout.setBottom(textField);

        Scene scene = new Scene(layout, 800, 1000);
        primaryStage.setScene(scene);
    }

    public void updateRoomList(List<Map<String, Object>> rooms) {
        //구현 필요
    }
}