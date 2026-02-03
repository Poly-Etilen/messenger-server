package com.nhnacademy;

import com.nhnacademy.command.impl.LeaveRoomCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static org.mockito.Mockito.when;

public class RoomExitTest extends ServerTestSupport{

    @Test
    @DisplayName("방 나가기: 세션 정리, 현재 방 ID 초기화, 다른 멤버에게 퇴장 알림 전송 확인")
    void leaveRoomTest() throws IOException {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("Exit Room");
        session.setUserId("leaver");
        room.addSession(session);
        session.setCurrentRoomId(room.getId());
        SessionManager.getInstance().addSession("leaver", session);

        ByteArrayOutputStream observerOut = new ByteArrayOutputStream();
        ClientSession observerSession = createMockSession("observer", observerOut);
        room.addSession(observerSession);
        SessionManager.getInstance().addSession("observer", observerSession);

        LeaveRoomCommand command = new LeaveRoomCommand();
        injectDependencies(command);

        Message request = createMessage(MessageType.CHAT_ROOM_EXIT, Map.of("roomId", room.getId()));
        command.execute(request);

        Assertions.assertNull(session.getCurrentRoomId(), "나간 후에는 현재 방 ID가 null이어야 합니다.");
        Assertions.assertFalse(room.getSessions().contains(session), "방의 세션 목록에서 제거되어야 합니다.");
        Assertions.assertTrue(out.size() > 0, "성공 응답을 받아야 합니다.");

        String broadcastMsg = observerOut.toString();

        System.out.println("DEBUG JSON Output: " + broadcastMsg);
        Assertions.assertTrue(broadcastMsg.contains("leaver"), "퇴장한 유저의 ID가 알림에 포함되어야 합니다.");
        Assertions.assertTrue(broadcastMsg.contains("PUSH-ROOM-EXIT"), "메시지 타입이 PUSH-ROOM-EXIT 여야 합니다.");

        Assertions.assertTrue(broadcastMsg.contains(room.getId()), "알림 메시지에 방 ID가 포함되어야 합니다.");
    }

    private ClientSession createMockSession(String userId, ByteArrayOutputStream outputStream) throws IOException {
        Socket mockSocket = Mockito.mock(Socket.class);
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        ClientSession session = new ClientSession(mockSocket, null, null);
        session.setUserId(userId);
        return session;
    }
}
