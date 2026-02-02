package com.nhnacademy.command.impl;

import com.nhnacademy.annotation.LoginRequired;
import com.nhnacademy.command.Command;
import com.nhnacademy.constant.MessageKey;
import com.nhnacademy.context.SessionHolder;
import com.nhnacademy.domain.Header.MessageHeader;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.domain.payload.MessagePayload;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@LoginRequired
public class RoomUserListCommand implements Command {
    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);

        if (roomId == null) {
            roomId = session.getCurrentRoomId();
        }

        ChatRoom room = ChatRoomManager.getInstance().getRoom(roomId);
        if (room == null) {
            return;
        }

        List<String> userList = new ArrayList();
        for (ClientSession member : room.getSessions()) {
            if (member.getUserId() != null) {
                userList.add(member.getUserId());
            }
        }

        MessageHeader header = new MessageHeader(MessageType.ROOM_USER_LIST_RESPONSE, LocalDateTime.now());

        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put("userList", userList);

        Message response = new Message("0", header, payload);

        try {
            MessageCodec.sendMessage(session.getSocket().getOutputStream(), response);
            log.info("방 멤버 목록 전송: room={}, requester={}", roomId, session.getUserId());
        } catch (IOException e) {
            log.error("전송 실패", e);
        }
    }
}
