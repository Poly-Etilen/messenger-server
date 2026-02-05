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

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        // 서버가 소켓에 무언가 쓰면 실제 소켓 대신 out에 저장됨
        when(session.getObserver()).thenReturn(observer);
        // 세션 생성 및 컨텍스트 설정
        when(session.getUserId()).thenReturn("testUser");
        when(observer.getUserId()).thenReturn("testUser");
        SessionHolder.set(session);
    }

    @AfterEach
    void teardown() throws Exception {
        // 스레드 로컬 비우기
        SessionHolder.clear();
        closeable.close();
    }

    protected void injectDependencies(Object command) {
        try {
            // command 객체의 모든 필드를 순회함
            for (Field field : command.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                // @Inject가 붙은 필드만 대상으로 지정
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true); // private 필드 접근 가능하게 설정

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
        return  new Message(header, payload);
    }
}
