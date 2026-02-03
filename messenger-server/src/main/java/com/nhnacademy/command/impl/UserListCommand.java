package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.CommandMapping;
import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.session.ClientSession;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.USER_LIST)
public class UserListCommand implements Command {
    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        List<ClientSession> allSessions = SessionManager.getInstance().getAllSessions();

        List<Map<String, Object>> userListData = new ArrayList<>();
        for (ClientSession member : allSessions) {
            Map<String, Object> userMap = new HashMap<>();
            String userId = member.getUserId();

            userMap.put("id", userId);
            userMap.put("name", userId);
            userMap.put("online", true);

            userListData.add(userMap);
        }

        MessageHeader header = new MessageHeader(MessageType.USER_LIST_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.USER_LIST, userListData);

        session.sendMessage(new Message("0", header, payload));
    }
}
