package com.nhnacademy.command;

import com.nhnacademy.command.impl.*;
import com.nhnacademy.domain.Header.MessageType;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    public Map<MessageType, Command> createCommandMap() {
        Map<MessageType, Command> commandMap = new HashMap<>();
        commandMap.put(MessageType.LOGIN, new LoginCommand());
        commandMap.put(MessageType.LOGOUT, new LogoutCommand());
        commandMap.put(MessageType.CHAT_ROOM_CREATE, new CreateRoomCommand());
        commandMap.put(MessageType.CHAT_ROOM_ENTER, new JoinRoomCommand());
        commandMap.put(MessageType.CHAT_ROOM_EXIT, new LeaveRoomCommand());
        commandMap.put(MessageType.CHAT_ROOM_LIST, new ListRoomCommand());
        commandMap.put(MessageType.CHAT_MESSAGE, new SendMessageCommand());
        commandMap.put(MessageType.PRIVATE_MESSAGE, new WhisperMessageCommand());
        commandMap.put(MessageType.CHAT_MESSAGE_HISTORY, new MessageHistoryCommand());
        commandMap.put(MessageType.USER_LIST, new UserListCommand());
        commandMap.put(MessageType.FILE_TRANSFER, new FileTransferCommand());
        commandMap.put(MessageType.CHAT_ROOM_USER_LIST, new ChatRoomUserListCommand());

        return commandMap;
    }
}
