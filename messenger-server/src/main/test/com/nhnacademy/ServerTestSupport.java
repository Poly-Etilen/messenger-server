package com.nhnacademy;

import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.session.ClientSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
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
    protected Socket mockSocket;

    protected ClientSession session;
    protected ByteArrayOutputStream out;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        out = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(out);
        session = new ClientSession(mockSocket);
    }

    @AfterEach
    void teardown() {
        resetSingleton(SessionManager.getInstance(), "sessionMap");
        resetSingleton(ChatRoomManager.getInstance(), "roomMaps");
    }

    protected void resetSingleton(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            ((Map<?,?>) field.get(instance)).clear();
        } catch (Exception e) {
            throw new RuntimeException("싱글톤 초기화 실해: " + fieldName, e);
        }
    }

    protected Message createMessage(MessageType type, Map<String, Object> data) {
        MessageHeader header = new MessageHeader(type, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        if (data != null) {
            data.forEach((k,v)-> payload.getData().put(k, v));
        }
        return  new Message("0", header, payload);
    }
}
