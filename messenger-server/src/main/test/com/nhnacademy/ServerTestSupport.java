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
import com.nhnacademy.repository.UserRepository;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ServerTestSupport {

    protected ClientSession session;
    protected ByteArrayOutputStream out;
    protected Socket socket;
    protected UserRepository userRepository;

    @BeforeEach
    public void setup() throws IOException {
        // 가짜 소켓 생성
        socket = Mockito.mock(Socket.class);
        // 서버가 소켓에 무언가 쓰면 실제 소켓 대신 out에 저장됨
        out = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(out);
        // 세션 생성 및 컨텍스트 설정
        session = new ClientSession(socket, null, null);
        SessionHolder.set(session);
        userRepository = new UserRepository();
    }

    @AfterEach
    void teardown() {
        // 스레드 로컬 비우기
        SessionHolder.clear();

        // 싱글톤 객체 초기화
        resetSingleton(ChatRoomManager.getInstance(), "roomMaps");
        resetSingleton(SessionManager.getInstance(), "sessionMap");
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
                        field.set(command, ChatRoomManager.getInstance());
                    } else if (field.getType().isAssignableFrom(SessionManager.class)) {
                        field.set(command, SessionManager.getInstance());
                    } else if (field.getType().isAssignableFrom(UserRepository.class)) {
                        field.set(command, userRepository);
                    } else if (field.getType().isAssignableFrom(MessageQueueManager.class)) {
                        field.set(command, MessageQueueManager.getInstance());
                        MessageQueueManager.getInstance().start();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("테스트 의존성 주입 실패: " + command.getClass().getSimpleName(), e);
        }
    }

    protected void resetSingleton(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            // 맵을 꺼내서 clear() 호출해서 데이터를 싹 비움
            ((Map<?,?>) field.get(instance)).clear();
        } catch (Exception e) {
            throw new RuntimeException("싱글톤 초기화 실패: " + fieldName, e);
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
