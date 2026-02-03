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
import com.nhnacademy.exception.NotAuthorizedException;
import com.nhnacademy.exception.RoomNotFoundException;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@CommandMapping(MessageType.CHAT_ROOM_USER_LIST)
@LoginRequired
public class ChatRoomUserListCommand implements Command {
    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);

        ChatRoom room = chatRoomManager.getRoom(roomId);
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }
        if (!room.getSessions().contains(session)) {
            throw new NotAuthorizedException();
        }

        List<String> userList = room.getSessions().stream()
                .map(ClientSession::getUserId)
                .toList();
        sendSuccess(session, roomId, userList);
    }

    private void sendSuccess(ClientSession session, String roomId, List<String> userList) {
        MessageHeader messageHeader = new MessageHeader(MessageType.CHAT_ROOM_USER_LIST_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.USER_LIST, userList);
        Message response = new Message("0", messageHeader, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
        } catch (IOException e) {
            log.error("응답 전송 실패", e);
        }
    }
}
