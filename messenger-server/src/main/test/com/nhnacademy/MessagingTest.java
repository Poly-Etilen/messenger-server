package com.nhnacademy;

import com.nhnacademy.command.impl.SendMessageCommand;
import com.nhnacademy.command.impl.WhisperMessageCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessagingTest extends ServerTestSupport{

    @Test
    @DisplayName("채팅 메시지 전송: 같은 방은 다른 사용자에게 브로드 캐스트 되어야 함")
    void sendMessageTest() throws IOException {
        ChatRoom room = ChatRoomManager.getInstance().createRoom("Chat Room");
        session.setUserId("sender");
        room.addSession(session);

        Socket receiverSocket = mock(Socket.class);
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        when(receiverSocket.getOutputStream()).thenReturn(receiverOut);

        ClientSession receiverSession = new ClientSession(receiverSocket);
        receiverSession.setUserId("receiver");
        room.addSession(receiverSession);

        SendMessageCommand command = new SendMessageCommand();
        Message message = createMessage(MessageType.CHAT_MESSAGE, Map.of(
                "roomId", room.getId(),
                "message", "Hello World!"
        ));

        command.execute(message);
        Assertions.assertTrue(receiverOut.size() > 0, "상대방에게 메시지가 전송되어야 합니다.");
        Assertions.assertTrue(out.size() > 0, "본인에게 전송 성공 응답이 와야 합니다.");
    }

    @Test
    @DisplayName("귓속말 전송: 특정 사용자에게만 메시지가 전달되어야 함")
    void whisperMessageTest() throws IOException {
        session.setUserId("sender");
        SessionManager.getInstance().addSession("sender", session);

        Socket receiverSocket = mock(Socket.class);
        ByteArrayOutputStream receiverOut = new ByteArrayOutputStream();
        when(receiverSocket.getOutputStream()).thenReturn(receiverOut);

        ClientSession receiverSession = new ClientSession(receiverSocket);
        receiverSession.setUserId("receiver");
        SessionManager.getInstance().addSession("receiver", receiverSession);

        WhisperMessageCommand command = new WhisperMessageCommand();
        Message message = createMessage(MessageType.PRIVATE_MESSAGE, Map.of(
                "receiverId", "receiver",
                "message", " Secret Message"
        ));

        command.execute(message);

        Assertions.assertTrue(receiverOut.size() > 0, "수신자에게 귓속말이 전달되어야 합니다.");
        Assertions.assertTrue(out.size() > 0, "발신자에게 성공 응답이 와야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 방에 메시지 전송 시 실패 처리")
    void sendMessageFailTest() {
        SendMessageCommand command = new SendMessageCommand();
        Message request = createMessage(MessageType.CHAT_MESSAGE, Map.of(
                "roomId", "invalid-room-id",
                "message", "Hello World!"
        ));

        command.execute(request);

        Assertions.assertEquals(0, out.size(), "존재하지 않는 방에는 메시지를 보낼 수 없습니다.");
    }
}
