package com.nhnacademy;

import com.nhnacademy.command.impl.JoinRoomCommand;
import com.nhnacademy.command.impl.UserListCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.Socket;
import java.util.Map;

public class RoomParticipationTest extends ServerTestSupport{

    @Test
    @DisplayName("방 입장: 방 세션 목록에 추가되어야 함")
    void joinRoomTest() {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("Game Room");
        JoinRoomCommand command = new JoinRoomCommand();
        injectDependencies(command);
        Message message = createMessage(MessageType.CHAT_ROOM_ENTER, Map.of("roomId", room.getId()));

        command.execute(message);

        Assertions.assertTrue(room.getSessions().contains(session));
        Assertions.assertTrue(out.size() > 0, "입장 성공 메시지가 전송되어야 함");
    }

    @Test
    @DisplayName("방 입장 실패: 존재하지 않는 방 ID 입력 시 실패 응답 전송")
    void joinRoomFailTest() {
        JoinRoomCommand command = new JoinRoomCommand();
        injectDependencies(command);
        Message message = createMessage(MessageType.CHAT_ROOM_ENTER, Map.of("roomId", "invalid"));
        Assertions.assertThrows(RoomNotFoundException.class, () -> command.execute(message));
    }

    @Test
    @DisplayName("방 유저 목록 조회: 현재 방 인원 정보 전송")
    void userListTest() {
        session.setUserId("marco");
        SessionManager.getInstance().addSession("marco", session);

        ClientSession otherSession = new ClientSession(Mockito.mock(Socket.class), null, null);
        otherSession.setUserId("alice");
        SessionManager.getInstance().addSession("alice", otherSession);

        UserListCommand command = new UserListCommand();
        injectDependencies(command);
        Message message = createMessage(MessageType.USER_LIST, Map.of());

        command.execute(message);

        String response = out.toString();

        Assertions.assertTrue(out.size() > 0, "응답이 전송되어야 합니다.");
        Assertions.assertTrue(response.contains("marco"), "응답에 'marco'가 포함되어야 합니다.");
        Assertions.assertTrue(response.contains("alice"), "응답에 'alice'이 포함되어야 합니다.");
        Assertions.assertTrue(response.contains("bob"), "응답에 'bob'이 포함되어야 합니다.");
    }
}
