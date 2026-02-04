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

public class PayloadExtractorTest {
    private Message createMessage(String key, String value) {
        MessageHeader header = new MessageHeader(MessageType.LOGIN, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        if (key != null) {
            payload.getData().put(key, value);
        }
        return new Message(header, payload);
    }

    @Test
    @DisplayName("필수 파라미터가 존재할 경우 값 반환")
    void getRequired_Success() {
        Message message = createMessage(MessageKey.USER_ID, "marco");
        String result = PayloadExtractor.getRequired(message, MessageKey.USER_ID);
        Assertions.assertEquals("marco", result);
    }

    @Test
    @DisplayName("필수 파라미터 누락 시 예외 발생")
    void getRequired_Fail_Null() {
        Message message = createMessage("otherKey", "value");
        Assertions.assertThrows(InvalidRequestException.class, () -> PayloadExtractor.getRequired(message, MessageKey.USER_ID));
    }

    @Test
    @DisplayName("필수 파라미터가 빈 문자열일 경우 예외 발생")
    void getRequired_Fail_Empty() {
        Message message = createMessage(MessageKey.USER_ID, "  ");
        Assertions.assertThrows(InvalidRequestException.class, () -> PayloadExtractor.getRequired(message, MessageKey.USER_ID));
    }
}
