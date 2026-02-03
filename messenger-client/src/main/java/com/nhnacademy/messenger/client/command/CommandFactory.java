package com.nhnacademy.messenger.client.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory{
    private final Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }


    public Command get(String name) {
        return commands.get(name);
    }
}

