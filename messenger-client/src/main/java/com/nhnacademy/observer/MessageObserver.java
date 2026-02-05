package com.nhnacademy.observer;

import com.nhnacademy.domain.Header.MessageType;

import java.util.Map;

public interface MessageObserver {
    void execute(MessageType type, Map<String, Object> data);
}