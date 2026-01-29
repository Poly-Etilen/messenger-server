package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.ui.ClientGUI;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientEventHandler {
    private final ClientGUI view;

    public ClientEventHandler(ClientGUI view) {
        this.view = view;
    }

    public void onLoginClicked(String id ,String password) {
        log.debug("user id: {} user password: {}",id,password);
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

}
