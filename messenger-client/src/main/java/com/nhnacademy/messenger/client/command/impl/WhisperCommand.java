package com.nhnacademy.messenger.client.command.impl;

import com.nhnacademy.messenger.client.command.Command;
import com.nhnacademy.messenger.client.handler.ClientEventHandler;
import com.nhnacademy.ui.form.impl.ClientGUI;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WhisperCommand implements Command {
    private final ClientEventHandler handler;

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
           return;
        }
        handler.sendWhisperMessage(args[1], args[2]);
    }
}
