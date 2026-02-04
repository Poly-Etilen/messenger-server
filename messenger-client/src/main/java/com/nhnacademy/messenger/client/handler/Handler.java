package com.nhnacademy.messenger.client.handler;

import com.nhnacademy.domain.Message;

public interface Handler {
    void handler(Message message);
}
