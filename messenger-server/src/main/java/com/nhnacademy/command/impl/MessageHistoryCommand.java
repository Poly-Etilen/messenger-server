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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@LoginRequired
public class MessageHistoryCommand implements Command {
    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        ChatRoom room = ChatRoomManager.getInstance().getRoom(roomId);

        if (room == null) {
            log.error("기록 조회 실패: 존재하지 않는 방 ({})", roomId);
            return;
        }

        List<Map<String, String>> historyData = new ArrayList<>();

        for (ChatRoom.StoredMessage msg : room.getMessageLog()) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("senderId", msg.getSenderId());
            msgMap.put("message", msg.getContent());
            msgMap.put("timestamp", msg.getTimestamp().toString());
            historyData.add(msgMap);
        }

        MessageHeader messageHeader = new MessageHeader(MessageType.MESSAGE_HISTORY_RESPONSE, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("roomId", roomId);
        payload.getData().put("history", historyData);
        Message response = new Message("0", messageHeader, payload);

        try {
            MessageCodec.sendMessage(session.getOutputStream(), response);
            log.info("메시지 기록 전송 완료: room={}, count={}", roomId, historyData.size());
        } catch (IOException e) {
            log.error("메시지 기록 전송 실패", e);
        }
    }
}
