package com.nhnacademy.messenger.client.command.impl;

import com.nhnacademy.messenger.client.command.Command;
import com.nhnacademy.messenger.client.handler.ClientEventHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LogoutCommand implements Command {
    ClientEventHandler handler;

    @Override
    public void execute(String[] args) {
        handler.onLogoutClicked();

    }
}
