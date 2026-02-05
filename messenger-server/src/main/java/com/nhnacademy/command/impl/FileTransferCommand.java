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
import com.nhnacademy.observer.MessageObserver;
import com.nhnacademy.session.ClientSession;
import com.nhnacademy.util.PayloadExtractor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@LoginRequired
@CommandMapping(MessageType.FILE_TRANSFER)
public class FileTransferCommand implements Command {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    @Inject
    private ChatRoomManager chatRoomManager;

    @Override
    public void execute(Message request) {
        ClientSession session = SessionHolder.get();

        // 요청으로부터 방ID, 파일 이름, 파일 데이터를 추출함
        String roomId = PayloadExtractor.getRequired(request, MessageKey.ROOM_ID);
        String fileName = PayloadExtractor.getRequired(request, MessageKey.FILE_NAME);
        String fileData = PayloadExtractor.getRequired(request, MessageKey.FILE_DATA);

        // Base64 인코딩은 바이너리 데이터 3바이트를 4개로 변환함. -> 용량이 33% 늘어남
        // 원본 파일의 크기를 알기 위해선 문자열 길이의 * 0.75 (3/4)를 계산하여 역추적함
        long estimatedSize = (long) (fileData.length() * 0.75);

        // 10MB 이상의 파일인 거부함
        if (estimatedSize > MAX_FILE_SIZE) {
            log.warn("파일 전송 실패: 용량 초과 (User={}, Size={} bytes)", session.getUserId(), estimatedSize);
            session.getObserver().sendMessage(createErrorMessage("FILE.SIZE_EXCEEDED", "파일 크기가 10MB를 초과했습니다."));
            return;
        }

        ChatRoom room = chatRoomManager.getRoom(roomId);
        if (room == null) {
            session.getObserver().sendMessage(createErrorMessage("ROOM.NOT_FOUND", "참여 중인 방이 아닙니다."));
            return;
        }

        Message broadcastMessage = createBroadcastMessage(session.getUserId(), roomId, fileName, fileData);

        for (MessageObserver member : room.getSessions()) {
            if (!member.getUserId().equals(session.getUserId())) {
                member.sendMessage(broadcastMessage);
            }
        }

        sendSuccessResponse(session, roomId, fileName);
        log.info("파일 전송 완료: sender={}, file={}", session.getUserId(), fileName);
    }

    private Message createBroadcastMessage(String senderId, String roomId, String fileName, String fileData) {
        MessageHeader header = new MessageHeader(MessageType.FILE_TRANSFER, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();

        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.SENDER_ID, senderId);
        payload.getData().put(MessageKey.FILE_NAME, fileName);
        payload.getData().put(MessageKey.FILE_DATA, fileData);

        return new Message(header, payload);
    }

    private void sendSuccessResponse(ClientSession session, String roomId, String fileName) {
        MessageHeader header = new MessageHeader(MessageType.FILE_TRANSFER_SUCCESS, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put(MessageKey.ROOM_ID, roomId);
        payload.getData().put(MessageKey.FILE_NAME, fileName);
        payload.getData().put(MessageKey.RESULT, "ok");

        session.getObserver().sendMessage(new Message(header, payload));
    }

    private Message createErrorMessage(String code, String message) {
        MessageHeader header = new MessageHeader(MessageType.ERROR, LocalDateTime.now());
        MessagePayload payload = new MessagePayload();
        payload.getData().put("code", code);
        payload.getData().put(MessageKey.REASON, message);
        return new Message(header, payload);
    }
}
