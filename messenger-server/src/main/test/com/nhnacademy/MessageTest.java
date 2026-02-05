package com.nhnacademy;

import com.nhnacademy.command.impl.FileTransferCommand;
import com.nhnacademy.command.impl.MessageHistoryCommand;
import com.nhnacademy.command.impl.SendMessageCommand;
import com.nhnacademy.command.impl.WhisperMessageCommand;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.BroadcastMessage;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageTest extends ServerTestSupport {

    @Test
    @DisplayName("메시지 전송: 같은 방의 다른 유저에게 전달되어야 함")
    void sendMessageTest() throws IOException, InterruptedException {
        String roomId = "chat-room-id";
        ChatRoom room = new ChatRoom(roomId, "Chat Room");

        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        // Sender 설정
        when(session.getUserId()).thenReturn("sender");
        when(observer.getUserId()).thenReturn("sender");
        room.addSession(session.getObserver());

        // Receiver 설정
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        ClientSession receiver = createMockSession("receiver", receiverOut);
        room.addSession(receiver.getObserver());

        SendMessageCommand command = new SendMessageCommand();
        injectDependencies(command);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                BroadcastMessage msg = invocation.getArgument(0);
                // 실제 서버 로직(MessageWorker)이 하는 일을 흉내냄
                msg.getChatRoom().notifyObservers(msg);
                return null;
            }
        }).when(messageQueueManager).submit(any(BroadcastMessage.class));

        // When
        command.execute(createMessage(MessageType.CHAT_MESSAGE, Map.of(
                "roomId", roomId,
                "message", "Hello World"
        )));

        // Then
        String output = receiverOut.toString();
        Assertions.assertTrue(output.contains("Hello World"), "수신자에게 메시지가 전달되어야 합니다.");
    }

    @Test
    @DisplayName("귓속말: 특정 유저에게만 메시지가 전달되어야 함")
    void whisperMessageTest() throws IOException {
        // 1. Sender (보내는 사람) 설정 - Mock Session
        when(session.getUserId()).thenReturn("sender");
        when(observer.getUserId()).thenReturn("sender");

        // 2. Receiver (받는 사람) 설정 - Real ClientSession with Mock Socket
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        ClientSession receiver = createMockSession("receiver", receiverOut);

        when(sessionManager.getSession("receiver")).thenReturn(receiver);

        WhisperMessageCommand command = new WhisperMessageCommand();
        injectDependencies(command);

        // When
        command.execute(createMessage(MessageType.PRIVATE_MESSAGE, Map.of(
                "receiverId", "receiver",
                "message", "Secret"
        )));

        // Then
        String log = receiverOut.toString();
        Assertions.assertTrue(log.contains("Secret"), "수신자에게 비밀 메시지가 전달되어야 합니다.");
        Assertions.assertTrue(log.contains("sender"), "보낸 사람의 ID가 포함되어야 합니다.");
    }

    @Test
    @DisplayName("파일 전송: Base64 데이터가 포함된 메시지가 브로드캐스트 되어야 함")
    void fileTransferTest() throws IOException {
        // Given
        String roomId = "file-room-id";
        ChatRoom room = new ChatRoom(roomId, "File Room"); // 방 객체 직접 생성

        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        // Sender(보내는 사람) 설정
        when(session.getUserId()).thenReturn("sender");
        when(observer.getUserId()).thenReturn("sender");
        room.addSession(session.getObserver());

        // Receiver(받는 사람) 설정
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        ClientSession receiver = createMockSession("receiver", receiverOut);
        room.addSession(receiver.getObserver());

        FileTransferCommand command = new FileTransferCommand();
        injectDependencies(command);

        String dummyData = "ZW5jb2RlZERhdGE=";

        // When
        command.execute(createMessage(MessageType.FILE_TRANSFER, Map.of(
                "roomId", roomId,
                "fileName", "test.txt",
                "fileData", dummyData
        )));

        // Then
        String broadcast = receiverOut.toString();
        Assertions.assertTrue(broadcast.contains("test.txt"), "파일명이 전송되어야 합니다.");
        Assertions.assertTrue(broadcast.contains(dummyData), "파일 데이터가 전송되어야 합니다.");
    }

    @Test
    @DisplayName("메시지 기록 조회: 저장된 대화 내용을 반환해야 함")
    void messageHistoryTest() {
        // Given
        String roomId = "history-room-id";
        ChatRoom room = new ChatRoom(roomId, "History Room"); // 방 객체 직접 생성
        room.addMessage("user1", "First Message");
        room.addMessage("user2", "Second Message");

        when(chatRoomManager.getRoom(roomId)).thenReturn(room);

        MessageHistoryCommand command = new MessageHistoryCommand();
        injectDependencies(command);

        // When
        command.execute(createMessage(MessageType.CHAT_MESSAGE_HISTORY, Map.of("roomId", roomId)));

        // Then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(observer).sendMessage(messageCaptor.capture());

        Message response = messageCaptor.getValue();
        Assertions.assertEquals(MessageType.CHAT_MESSAGE_HISTORY_SUCCESS, response.getHeader().getMessageType());

        // Payload 검증
        List<Map<String, String>> history = (List<Map<String, String>>) response.getPayload().getData().get(MessageKey.HISTORY);
        Assertions.assertNotNull(history);
        Assertions.assertEquals(2, history.size());

        // 내용 확인
        boolean hasFirstMsg = history.stream().anyMatch(msg -> "First Message".equals(msg.get("message")));
        boolean hasSecondMsg = history.stream().anyMatch(msg -> "Second Message".equals(msg.get("message")));

        Assertions.assertTrue(hasFirstMsg, "첫 번째 메시지가 포함되어야 합니다.");
        Assertions.assertTrue(hasSecondMsg, "두 번째 메시지가 포함되어야 합니다.");
    }
}