package com.nhnacademy.command.impl;

import com.google.inject.Inject;
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
import com.nhnacademy.repository.UserRepository;
import com.nhnacademy.session.ClientSession;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.USER_LIST)
public class UserListCommand implements Command {
    @Inject
    private SessionManager sessionManager;
    @Inject
    private UserRepository userRepository;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        Set<String> allUserIds = userRepository.getAllUserIds();

        List<Map<String, Object>> userListData = new ArrayList<>();
        for (String userId : allUserIds) {
            Map<String, Object> userMap = new HashMap<>();

            boolean isOnline = sessionManager.isLoggedIn(userId);

            userMap.put("id", userId);
            userMap.put("name", userId);
            userMap.put("online", isOnline);

            userListData.add(userMap);
        }

        MessageHeader header = new MessageHeader(MessageType.USER_LIST_SUCCESS, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.USER_LIST, userListData);

        session.getObserver().sendMessage(new Message(header, payload));
    }
}
