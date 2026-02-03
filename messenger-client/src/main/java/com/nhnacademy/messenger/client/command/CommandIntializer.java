package com.nhnacademy.messenger.client.command;

import com.nhnacademy.messenger.client.command.impl.ExitCommand;
import com.nhnacademy.messenger.client.command.impl.WhisperCommand;
import com.nhnacademy.messenger.client.handler.ClientEventHandler;

public class CommandIntializer {
    public static CommandFactory init(ClientEventHandler handler) {

        CommandFactory factory = new CommandFactory();

        factory.register("/whisper", new WhisperCommand(handler));
        factory.register("/exit", new ExitCommand(handler));
        factory.register("/logout",new ExitCommand(handler));

        return factory;
    }
}
