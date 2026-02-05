package com.nhnacademy.messenger.client.event.listener;

import java.util.List;
import java.util.Map;

public interface ClientEventListener {

    //상태 변경
    void onLoginSuccess(String userId);
    void onLogout();

    //화면 변경
    void onShowRoomList();
    void onEnterRoom();

    //조회
    String getRoomIdByName(String roomname);




    void writeMessage(String message);

    //화면 동기화
    void updateRoomUserList(List<String> roomUserList);
    void updateUserList(List<Map<String, Object>> userListData);
    void updateRoomList(List<Map<String, Object>> rooms);

    //에러
    void showError(String title, String content);

}
