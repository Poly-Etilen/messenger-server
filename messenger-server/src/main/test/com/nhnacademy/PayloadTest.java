package com.nhnacademy;

import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.exception.InvalidRequestException;
import com.nhnacademy.util.PayloadExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PayloadTest {

    private Message createMessage(String key, String value) {
        MessageHeader header = new MessageHeader(MessageType.LOGIN, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        if (key != null) {
            payload.getData().put(key, value);
        }
        return new Message(header, payload);
    }

    @Test
    @DisplayName("필수 파라미터 추출 성공")
    void getRequiredSuccess() {
        Message message = createMessage(MessageKey.USER_ID, "marco");
        String result = PayloadExtractor.getRequired(message, MessageKey.USER_ID);
        Assertions.assertEquals("marco", result);
    }

    @Test
    @DisplayName("필수 파라미터 누락 시 예외 발생")
    void getRequiredFailNull() {
        Message message = createMessage("wrongKey", "value");
        Assertions.assertThrows(InvalidRequestException.class, 
            () -> PayloadExtractor.getRequired(message, MessageKey.USER_ID));
    }

    @Test
    @DisplayName("필수 파라미터가 빈 값일 경우 예외 발생")
    void getRequiredFailEmpty() {
        Message message = createMessage(MessageKey.USER_ID, "   ");
        Assertions.assertThrows(InvalidRequestException.class, 
            () -> PayloadExtractor.getRequired(message, MessageKey.USER_ID));
    }
}