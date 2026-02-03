package com.nhnacademy;

import com.nhnacademy.command.impl.ChatRoomUserListCommand;
import com.nhnacademy.command.impl.FileTransferCommand;
import com.nhnacademy.command.impl.MessageHistoryCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
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

public class AdditionalFeatureTest extends ServerTestSupport{

    @Test
    @DisplayName("채팅 기록 조회: 저장된 메시지가 올바르게 반환되는지 확인")
    void messageHistoryTest() {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("History Room");
        room.addMessage("user1", "Hello");
        room.addMessage("user2", "Hi there");

        MessageHistoryCommand command = new MessageHistoryCommand();
        injectDependencies(command);

        Message request = createMessage(MessageType.CHAT_MESSAGE_HISTORY, Map.of("roomId", room.getId()));

        command.execute(request);

        String response = out.toString();
        Assertions.assertTrue(response.contains("Hello"));
        Assertions.assertTrue(response.contains("Hi there"));
        Assertions.assertTrue(response.contains("user1"));
        Assertions.assertTrue(response.contains("user2"));
    }

    @Test
    @DisplayName("파일 전송: 같은 방 멤버에게 파일 데이터 브로드캐스트 확인")
    void fileTransferTest() throws IOException {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("File Room");
        session.setUserId("sender");
        room.addSession(session);

        // 수신자 설정
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        ClientSession receiver = createMockSession("receiver", receiverOut);
        room.addSession(receiver);

        FileTransferCommand command = new FileTransferCommand();
        injectDependencies(command);

        String dummyFileData = "VGhpcyBpcyBhIHRlc3QgZmlsZSBkYXRh"; // Base64 dummy
        Message request = createMessage(MessageType.FILE_TRANSFER, Map.of(
                "roomId", room.getId(),
                "fileName", "test.txt",
                "fileData", dummyFileData
        ));

        command.execute(request);

        String broadcast = receiverOut.toString();
        Assertions.assertTrue(broadcast.contains("test.txt"));
        Assertions.assertTrue(broadcast.contains(dummyFileData));

        Assertions.assertTrue(out.toString().contains("ok"));
    }

    @Test
    @DisplayName("방 멤버 목록 조회: 현재 방에 참여 중인 유저 ID 반환 확인")
    void chatRoomUserListTest() throws IOException {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("User List Room");

        // 본인 입장
        session.setUserId("me");
        room.addSession(session);

        // 타인 입장
        ClientSession other = createMockSession("other", new ByteArrayOutputStream());
        room.addSession(other);

        ChatRoomUserListCommand command = new ChatRoomUserListCommand();
        injectDependencies(command);

        Message request = createMessage(MessageType.CHAT_ROOM_USER_LIST, Map.of("roomId", room.getId()));

        command.execute(request);

        String response = out.toString();
        Assertions.assertTrue(response.contains("me"));
        Assertions.assertTrue(response.contains("other"));
    }

    private ClientSession createMockSession(String userId, ByteArrayOutputStream outputStream) throws IOException {
        Socket mockSocket = Mockito.mock(Socket.class);
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        ClientSession session = new ClientSession(mockSocket, null);
        session.setUserId(userId);
        return session;
    }
}
