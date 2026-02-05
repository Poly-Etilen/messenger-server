package com.nhnacademy.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.domain.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NioMessageCodec {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String LENGTH_PREFIX = "message-length";

    static  {
        mapper.registerModule(new JavaTimeModule());
    }

    public static List<Message> decode(ByteBuffer buffer) throws IOException {
        List<Message> messages = new ArrayList<>();
        buffer.flip();

        while (buffer.hasRemaining()) {
            buffer.mark();

            String headerLine = readLine(buffer);
            if (headerLine == null) {
                buffer.reset();
                break;
            }

            try {
                int contentLength = parseContentLength(headerLine);

                if (buffer.remaining() < contentLength) {
                    buffer.reset();
                    break;
                }

                byte[] bodyBytes = new byte[contentLength];
                buffer.get(bodyBytes);
                String jsonPayload = new String(bodyBytes, StandardCharsets.UTF_8);
                Message message = mapper.readValue(jsonPayload, Message.class);
                messages.add(message);
            } catch (NumberFormatException | JsonProcessingException e) {
                throw new IOException("protocol parsing error", e);
            }
        }

        buffer.compact();
        return messages;
    }

    public static ByteBuffer encode(Message message) throws JsonProcessingException {
        String jsonPayload = mapper.writeValueAsString(message);
        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        String header = LENGTH_PREFIX + ":" + payloadBytes.length + "\n";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + payloadBytes.length);
        buffer.put(headerBytes);
        buffer.put(payloadBytes);

        buffer.flip();
        return buffer;
    }

    private static String readLine(ByteBuffer buffer) {
        int startPos = buffer.position();
        int endPos = -1;

        for (int i = startPos; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                endPos = i;
                break;
            }
        }

        if (endPos == -1) return null;

        int length = endPos - startPos + 1;
        byte[] lineBytes = new byte[length];
        buffer.get(lineBytes);

        return new String(lineBytes, StandardCharsets.UTF_8);
    }

    private static int parseContentLength(String line) {
        String cleanLine = line.trim();

        // 1. BOM(Byte Order Mark) 제거 (안전장치)
        cleanLine = cleanLine.replace("\uFEFF", "");

        // 2. 콜론(:)을 기준으로 분리하여 정확히 파싱
        String[] parts = cleanLine.split(":");

        // 형식 검증: "키:값" 형태여야 하며, 키가 일치해야 함
        if (parts.length != 2 || !parts[0].trim().equals(LENGTH_PREFIX)) {
            throw new IllegalArgumentException("Invalid header format: " + line);
        }

        try {
            return Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid content length value: " + parts[1]);
        }
    }
}
