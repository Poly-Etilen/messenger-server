package com.nhnacademy.messenger.client.command;

import com.nhnacademy.messenger.client.handler.ClientEventHandler;

public interface ClientCommand {
    void execute(ClientEventHandler handler, String[] args);
}
