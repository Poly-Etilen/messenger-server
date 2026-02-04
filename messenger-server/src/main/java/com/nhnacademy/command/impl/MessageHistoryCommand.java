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
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.MessageCodec;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.CHAT_MESSAGE_HISTORY)
public class MessageHistoryCommand implements Command {
    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        ChatRoom room = chatRoomManager.getRoom(roomId);

        if (room == null) {
            log.error("기록 조회 실패: 존재하지 않는 방 ({})", roomId);
            return;
        }

        List<Map<String, String>> historyData = new ArrayList<>();

        for (ChatRoom.StoredMessage msg : room.getMessageLog()) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put(MessageKey.SENDER_ID, msg.getSenderId());
            msgMap.put(MessageKey.MESSAGE, msg.getContent());
            msgMap.put(MessageKey.TIMESTAMP, msg.getTimestamp().toString());
            historyData.add(msgMap);
        }

        MessageHeader messageHeader = new MessageHeader(MessageType.CHAT_MESSAGE_HISTORY_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.HISTORY, historyData);
        Message response = new Message(messageHeader, payload);

        try {
            MessageCodec.sendMessage(session.getOutputStream(), response);
            log.info("메시지 기록 전송 완료: room={}, count={}", roomId, historyData.size());
        } catch (IOException e) {
            log.error("메시지 기록 전송 실패", e);
        }
    }
}
