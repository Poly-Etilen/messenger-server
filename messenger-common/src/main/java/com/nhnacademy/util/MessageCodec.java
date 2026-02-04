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
    private static final ObjectMapper mapper = new ObjectMapper(); // 자바 객체와 JSON 문자열 간의 변환
    private static final String LENGTH_PREFIX = "message-length:"; // 실제 데이터 길이를 알리는 헤더의 접두어

    static {
        mapper.registerModule(new JavaTimeModule()); //LocalDateTime 타입 처리
    }

    public static void sendMessage(OutputStream out, Message message) throws IOException {
        String jsonPayload = mapper.writeValueAsString(message); // 객체를 JSON 문자열로 변환
        byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8); //Byte 배열 변환
        int length = payloadBytes.length;

        String lengthLine = LENGTH_PREFIX + length + "\n"; // 수신 측에서 얼마만큼 데이터를 읽어야 할 지 알려줌
        byte[] HeaderBytes = lengthLine.getBytes(StandardCharsets.UTF_8);

        synchronized (out) { // 헤더를 먼저 쓰고 실제 데이터를 쓴 후 flush
            out.write(HeaderBytes);
            out.write(payloadBytes);
            out.flush();
        }
    }

    public static Message readMessage(InputStream in) throws IOException {
        String lengthLine = readLine(in); // 개행 문자가 나올때까지 읽음
        if (lengthLine == null) {
            return null;
        }

        int contentLength = parseContentLength(lengthLine);
        if (contentLength <= 0) {
            throw new IllegalArgumentException("Invalid message length: " + contentLength);
        }

        byte[] buffer = new byte[contentLength];
        int totalRead = 0;

        while (totalRead < contentLength) { // TCP 특성 상 read 호출로 모든 데이터가 오지 않을 수 있음
            int read = in.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected EOF while reading payload");
            }
            totalRead += read;
        }
        String jsonPayload = new String(buffer, StandardCharsets.UTF_8);
        return mapper.readValue(jsonPayload, Message.class); // 역직렬화
    }

    private static int parseContentLength(String line) throws IOException {
        if (!line.startsWith(LENGTH_PREFIX)) { // message-length로 시작하는지 검증
            throw new IllegalArgumentException("Invalid header format: " + line);
        }
        try { // 뒤의 숫자를 파싱해서 실제 데이터의 길이를 가져옴.
            return Integer.parseInt(line.substring(LENGTH_PREFIX.length()).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in header: " + line, e);
        }
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); // 1바이트씩 읽다가 개행 문자를 만나면 읽은 내용을 문자열로 변환
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
