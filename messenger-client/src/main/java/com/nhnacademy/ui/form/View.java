package com.nhnacademy.ui.form;

import java.util.List;
import java.util.Map;

public interface View {
    void mainView();
    void showRoomList();
    void showEnterRoom();
    void logout();
    void showError(String title, String content);
    void updateRoomList(List<Map<String, Object>> rooms);
    void createRoom();
}
