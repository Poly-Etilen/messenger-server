package com.nhnacademy.util;

import com.nhnacademy.domain.Message;
import com.nhnacademy.exception.InvalidRequestException;

public class PayloadExtractor {
    public static String getRequired(Message message, String key) {
        Object value = message.getPayload().getData().get(key);
        if (value == null || ((String) value).trim().isEmpty()) {
            throw new InvalidRequestException("필수 파라미터가 누락되었습니다: " + key);
        }
        return (String) value;
    }

    public static String getOptional(Message message, String key) {
        Object value = message.getPayload().getData().get(key);
        return value != null ? (String) value : null;
    }
}
