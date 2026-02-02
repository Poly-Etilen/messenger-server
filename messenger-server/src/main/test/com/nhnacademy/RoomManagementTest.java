package com.nhnacademy;

import com.nhnacademy.command.impl.CreateRoomCommand;
import com.nhnacademy.command.impl.ListRoomCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class RoomManagementTest extends ServerTestSupport{

    @Test
    @DisplayName("방 생성: 매니저에 방이 추가되어야 함")
    void createRoomTest() {
        CreateRoomCommand command = new CreateRoomCommand();
        Message message = createMessage(MessageType.CHAT_ROOM_CREATE, Map.of("roomName", "Study Room"));

        command.execute(message);

        Assertions.assertEquals(1, ChatRoomManager.getInstance().getAllRooms().size());
        ChatRoom room = ChatRoomManager.getInstance().getAllRooms().get(0);
        Assertions.assertEquals("Study Room", room.getName());
    }

    @Test
    @DisplayName("방 목록 조회: 응답이 전송되어야 함")
    void listRoomsTest() {
        ChatRoomManager.getInstance().createRoom("Room A");
        ChatRoomManager.getInstance().createRoom("Room B");

        ListRoomCommand command = new ListRoomCommand();
        Message req = createMessage(MessageType.CHAT_ROOM_LIST, Map.of());

        command.execute(req);

        Assertions.assertTrue(out.size() > 0, "방 목록 데이터가 전송되어야 함");
    }
}
