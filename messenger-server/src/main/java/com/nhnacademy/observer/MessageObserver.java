package com.nhnacademy.observer;

import com.nhnacademy.domain.Message;
import com.nhnacademy.model.BroadcastMessage;

public interface MessageObserver {
    void onMessage(BroadcastMessage message);

    void sendMessage(Message message);

    String getUserId();
}
