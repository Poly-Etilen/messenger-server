package com.nhnacademy;

import com.nhnacademy.command.impl.CreateRoomCommand;
import com.nhnacademy.command.impl.JoinRoomCommand;
import com.nhnacademy.command.impl.RoomUserListCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class RoomParticipationTest extends ServerTestSupport{

    @Test
    @DisplayName("방 입장: 방 세션 목록에 추가되어야 함")
    void joinRoomTest() {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("Game Room");
        JoinRoomCommand command = new JoinRoomCommand();
        Message message = createMessage(MessageType.JOIN_ROOM, Map.of("roomId", room.getId()));

        command.execute(message);

        Assertions.assertTrue(room.getSessions().contains(session));
        Assertions.assertTrue(out.size() > 0, "입장 성공 메시지가 전송되어야 함");
    }

    @Test
    @DisplayName("방 입장 실패: 존재하지 않는 방 ID 입력 시 실패 응답 전송")
    void joinRoomFailTest() {
        JoinRoomCommand command = new JoinRoomCommand();
        Message message = createMessage(MessageType.JOIN_ROOM, Map.of("roomId", "invalid"));
        Assertions.assertThrows(RoomNotFoundException.class, () -> command.execute(message));
    }

    @Test
    @DisplayName("방 유저 목록 조회: 현재 방 인원 정보 전송")
    void roomUserListTest() {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("Dev Room");
        session.setUserId("marco");
        session.setCurrentRoomId(room.getId());
        room.addSession(session);

        RoomUserListCommand command = new RoomUserListCommand();
        Message message = createMessage(MessageType.ROOM_USER_LIST, Map.of("roomId", room.getId()));

        command.execute(message);

        Assertions.assertTrue(out.size() > 0, "유저 목록이 전송되어야 함");
    }
}
