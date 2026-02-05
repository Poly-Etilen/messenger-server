package com.nhnacademy;

import com.google.inject.Inject;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.MessageQueueManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.repository.UserRepository;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ServerTestSupport {

    @Mock
    protected ClientSession session;
    @Mock
    protected MessageObserver observer;
    @Mock
    protected ChatRoomManager chatRoomManager;
    @Mock
    protected SessionManager sessionManager;
    @Mock
    protected UserRepository userRepository;
    @Mock
    protected MessageQueueManager messageQueueManager;
    @Mock
    protected Socket socket;

    // 서버의 응답을 캡처하기 위한 출력 스트림
    protected ByteArrayOutputStream out;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        
        // 출력 스트림 초기화
        out = new ByteArrayOutputStream();
        
        // 세션 및 소켓 Mock 설정
        when(session.getObserver()).thenReturn(observer);
        when(session.getSocket()).thenReturn(socket);
        when(socket.getOutputStream()).thenReturn(out);
        when(session.getUserId()).thenReturn("testUser");
        when(observer.getUserId()).thenReturn("testUser");

        // ThreadLocal 컨텍스트 설정
        SessionHolder.set(session);
        
        // Manager 싱글톤 초기화 (테스트 간 간섭 방지)
        // 실제 구현에 따라 리셋 로직이 다를 수 있으나, 여기서는 Mock 주입으로 해결
    }

    @AfterEach
    void teardown() throws Exception {
        SessionHolder.clear();
        closeable.close();
    }

    /**
     * 리플렉션을 사용하여 Command 객체에 Mock 의존성을 주입합니다.
     */
    protected void injectDependencies(Object command) {
        try {
            for (Field field : command.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    if (field.getType().isAssignableFrom(ChatRoomManager.class)) {
                        field.set(command, this.chatRoomManager);
                    } else if (field.getType().isAssignableFrom(SessionManager.class)) {
                        field.set(command, this.sessionManager);
                    } else if (field.getType().isAssignableFrom(UserRepository.class)) {
                        field.set(command, this.userRepository);
                    } else if (field.getType().isAssignableFrom(MessageQueueManager.class)) {
                        field.set(command, this.messageQueueManager);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("테스트 의존성 주입 실패: " + command.getClass().getSimpleName(), e);
        }
    }

    protected Message createMessage(MessageType type, Map<String, Object> data) {
        MessageHeader header = new MessageHeader(type, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        if (data != null) {
            payload.getData().putAll(data);
        }
        return new Message(header, payload);
    }
    
    // 테스트용 Mock 세션 생성 도우미
    protected ClientSession createMockSession(String userId, ByteArrayOutputStream outputStream) throws IOException {
        Socket mockSocket = Mockito.mock(Socket.class);
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        ClientSession mockSession = new ClientSession(mockSocket, null, null);
        mockSession.setUserId(userId);
        return mockSession;
    }
}