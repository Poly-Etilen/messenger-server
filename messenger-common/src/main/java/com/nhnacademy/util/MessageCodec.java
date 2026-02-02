package com.nhnacademy.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.domain.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MessageCodec {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String LENGTH_PREFIX = "message-length:";

    static {
        mapper.registerModule(new JavaTimeModule());
    }

    public static void sendMessage(OutputStream out, Message message) throws IOException {
        String jsonPayload = mapper.writeValueAsString(message); // 객체를 JSON 문자열로 변환
        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
        int length = payloadBytes.length;

        String lengthLine = LENGTH_PREFIX + length + "\n";
        byte[] HeaderBytes = lengthLine.getBytes(StandardCharsets.UTF_8);

        synchronized (out) {
            out.write(HeaderBytes);
            out.write(payloadBytes);
            out.flush();
        }
    }

    public static Message readMessage(InputStream in) throws IOException {
        String lengthLine = readLine(in);
        if (lengthLine == null) {
            return null;
        }

        int contentLength = parseContentLength(lengthLine);
        if (contentLength <= 0) {
            throw new IllegalArgumentException("Invalid message length: " + contentLength);
        }

        byte[] buffer = new byte[contentLength];
        int totalRead = 0;

        while (totalRead < contentLength) {
            int read = in.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading payload");
            }
            totalRead += read;
        }
        String jsonPayload = new String(buffer, StandardCharsets.UTF_8);
        return mapper.readValue(jsonPayload, Message.class);
    }

    private static int parseContentLength(String line) throws IOException {
        if (!line.startsWith(LENGTH_PREFIX)) {
            throw new IllegalArgumentException("Invalid header format: " + line);
        }
        try {
            return Integer.parseInt(line.substring(LENGTH_PREFIX.length()).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in header: " + line, e);
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                return buffer.toString(StandardCharsets.UTF_8);
            }
            buffer.write(b);
        }
        if (buffer.size() == 0) return null;
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
