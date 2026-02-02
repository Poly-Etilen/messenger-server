package com.nhnacademy.command;

import com.nhnacademy.command.impl.*;
import com.nhnacademy.domain.Header.MessageType;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    public Map<MessageType, Command> createCommandMap() {
        Map<MessageType, Command> commandMap = new HashMap<>();
        commandMap.put(MessageType.LOGIN, new LoginCommand());
        commandMap.put(MessageType.CREATE_ROOM, new CreateRoomCommand());
        commandMap.put(MessageType.ROOM_LIST, new ListRoomCommand());
        commandMap.put(MessageType.JOIN_ROOM, new JoinRoomCommand());
        commandMap.put(MessageType.LOGOUT, new LogoutCommand());
        commandMap.put(MessageType.ROOM_USER_LIST, new RoomUserListCommand());
        commandMap.put(MessageType.CHAT_MESSAGE, new SendMessageCommand());
        commandMap.put(MessageType.WHISPER_MESSAGE, new WhisperMessageCommand());
        commandMap.put(MessageType.MESSAGE_HISTORY, new MessageHistoryCommand());
        commandMap.put(MessageType.LEAVE_ROOM, new LeaveRoomCommand());

        return commandMap;
    }
}
