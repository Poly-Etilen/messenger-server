package com.nhnacademy;

import com.nhnacademy.command.impl.CreateRoomCommand;
import com.nhnacademy.command.impl.JoinRoomCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
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
    }
}
