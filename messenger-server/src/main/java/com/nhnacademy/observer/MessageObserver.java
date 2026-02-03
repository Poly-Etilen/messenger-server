package com.nhnacademy.observer;

import com.nhnacademy.model.BroadcastMessage;

public interface MessageObserver {
    void onMessage(BroadcastMessage message);
}
