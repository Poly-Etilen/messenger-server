package com.nhnacademy;

import com.nhnacademy.command.impl.*;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoomTest extends ServerTestSupport {

    @Test
    @DisplayName("방 생성: 매니저의 createRoom이 정상적으로 호출되어야 함")
    void createRoomTest() {
        CreateRoomCommand command = new CreateRoomCommand();
        injectDependencies(command);

        ChatRoom mockRoom = new ChatRoom("new-room-id", "Java Study");
        when(chatRoomManager.createRoom("Java Study")).thenReturn(mockRoom);

        command.execute(createMessage(MessageType.CHAT_ROOM_CREATE, Map.of("roomName", "Java Study")));

        verify(chatRoomManager).createRoom("Java Study");
    }

    @Test
    @DisplayName("방 목록 조회: 매니저에서 반환된 방 목록이 출력되어야 함")
    void listRoomTest() {
        // Given
        ChatRoom room1 = new ChatRoom("id1", "Room A");
        ChatRoom room2 = new ChatRoom("id2", "Room B");
        when(chatRoomManager.getAllRooms()).thenReturn(List.of(room1, room2));

        ListRoomCommand command = new ListRoomCommand();
        injectDependencies(command);

        // When
        command.execute(createMessage(MessageType.CHAT_ROOM_LIST, Map.of()));

        // Then
        verify(chatRoomManager).getAllRooms();

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(observer).sendMessage(messageCaptor.capture());

        Message sentMessage = messageCaptor.getValue();
        Assertions.assertNotNull(sentMessage.getPayload());
        Assertions.assertFalse(sentMessage.getPayload().getData().isEmpty(), "응답 데이터가 비어있지 않아야 합니다.");
    }

    @Test
    @DisplayName("방 입장: 세션이 방의 멤버로 추가되어야 함")
    void joinRoomTest() {
        String roomId = "test-room-id";
        ChatRoom room = new ChatRoom(roomId, "Game Room");
        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        JoinRoomCommand command = new JoinRoomCommand();
        injectDependencies(command);

        command.execute(createMessage(MessageType.CHAT_ROOM_ENTER, Map.of("roomId", roomId)));

        Assertions.assertTrue(room.getSessions().contains(session.getObserver()));
    }

    @Test
    @DisplayName("방 입장 실패: 존재하지 않는 방 ID")
    void joinRoomFailTest() {
        String invalidId = "invalid-id";
        when(chatRoomManager.getRoom(invalidId)).thenReturn(null);

        JoinRoomCommand command = new JoinRoomCommand();
        injectDependencies(command);

        Assertions.assertThrows(RoomNotFoundException.class, () ->
                command.execute(createMessage(MessageType.CHAT_ROOM_ENTER, Map.of("roomId", invalidId)))
        );
    }

    @Test
    @DisplayName("방 나가기: 방 멤버에서 제외되고 알림이 브로드캐스트 되어야 함")
    void leaveRoomTest() throws IOException {
        String roomId = "exit-room-id";
        ChatRoom room = new ChatRoom(roomId, "Exit Test Room");
        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        // Session ID 설정
        session.setUserId("leaver"); // Mock Setter
        when(session.getUserId()).thenReturn("leaver");
        when(observer.getUserId()).thenReturn("leaver");

        room.addSession(session.getObserver());
        session.setCurrentRoomId(roomId);

        // 관찰자 설정
        ByteArrayOutputStream observerOut = new ByteArrayOutputStream();
        ClientSession watcher = createMockSession("watcher", observerOut);
        room.addSession(watcher.getObserver());

        LeaveRoomCommand command = new LeaveRoomCommand();
        injectDependencies(command);

        command.execute(createMessage(MessageType.CHAT_ROOM_EXIT, Map.of("roomId", roomId)));

        Assertions.assertNull(session.getCurrentRoomId());
        Assertions.assertFalse(room.getSessions().contains(session.getObserver()));

        String broadcast = observerOut.toString();
        Assertions.assertTrue(broadcast.contains("leaver"));
        Assertions.assertTrue(broadcast.contains("PUSH-ROOM-EXIT"));
    }

    @Test
    @DisplayName("방 유저 목록 조회: 현재 방에 있는 유저 ID들을 반환해야 함")
    void chatRoomUserListTest() throws IOException {
        // Given
        String roomId = "userlist-room-id";
        ChatRoom room = new ChatRoom(roomId, "User List Room");
        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        // [핵심 수정] Mock 객체의 ID를 "me"로 강제 리턴하도록 설정
        when(observer.getUserId()).thenReturn("me");
        room.addSession(session.getObserver());

        ClientSession other = createMockSession("other", new ByteArrayOutputStream());
        room.addSession(other.getObserver());

        ChatRoomUserListCommand command = new ChatRoomUserListCommand();
        injectDependencies(command);

        // When
        command.execute(createMessage(MessageType.CHAT_ROOM_USER_LIST, Map.of("roomId", roomId)));

        // Then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(observer).sendMessage(messageCaptor.capture());

        Message sentMessage = messageCaptor.getValue();
        List<String> userList = (List<String>) sentMessage.getPayload().getData().get(MessageKey.USER_LIST);

        Assertions.assertNotNull(userList, "유저 리스트는 null이 아니어야 합니다.");
        Assertions.assertTrue(userList.contains("me"), "리스트에 'me'가 포함되어야 합니다. 실제: " + userList);
        Assertions.assertTrue(userList.contains("other"), "리스트에 'other'가 포함되어야 합니다.");
    }
}