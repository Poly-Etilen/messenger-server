package com.nhnacademy.messenger.client.request;

import com.nhnacademy.messenger.client.request.impl.*;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public class RequestFactory {

    public static Request broadCastRequest(String roomId, String myUserId, String message) {
        return new BroadCastRequest(roomId, myUserId, message);
    }

    public static Request chatHistoryRequest(String roomId) {
        return new ChatHistoryRequest(roomId);
    }

    public static Request createRoomRequest(String roomName) {
        return new CreateRoomRequest(roomName);
    }

    public static Request exitRoomRequest(String roomId) {
        return new ExitRoomRequest(roomId);
    }

    public static Request loginRequest(String id, String password) {
        return new LoginRequest(id, password);
    }

    public static Request logoutRequest() {
        return new LogoutRequest();
    }

    public static Request memberListRqeust() {
        return new MemberListRequest();
    }

    public static Request roomJoinRequest(String selectedRoomId) {
        return new RoomJoinRequest(selectedRoomId);
    }

    public static Request roomListRequest() {
        return new RoomListRequest();
    }

    public static Request roomMemberListRequest(String roomId) {
        return new RoomMemberListRequest(roomId);
    }

    public static Request whisperRequest(String targetId,String trim){
        return new WhisperRequest(targetId,trim);
    }


}
