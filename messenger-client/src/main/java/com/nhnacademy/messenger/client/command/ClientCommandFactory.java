package com.nhnacademy.messenger.client.command;

//import com.nhnacademy.messenger.client.command.impl.ChatHistoryCommand;
import com.nhnacademy.messenger.client.command.impl.ExitCommand;
//import com.nhnacademy.messenger.client.command.impl.UserListCommand;
//import com.nhnacademy.messenger.client.command.impl.WhisperCommand;

import java.util.HashMap;
import java.util.Map;

public class ClientCommandFactory {
    private static final Map<String, ClientCommand> commandMap = new HashMap<>();

    static {
//        commandMap.put("/w", new WhisperCommand());
//        commandMap.put("/귓", new WhisperCommand());
//        commandMap.put("/user", new UserListCommand());
//        commandMap.put("/list", new UserListCommand());
        commandMap.put("/exit", new ExitCommand());
        commandMap.put("/나가기", new ExitCommand());
//        commandMap.put("/history", new ChatHistoryCommand());
//        commandMap.put("/기록", new ChatHistoryCommand());
    }
}
