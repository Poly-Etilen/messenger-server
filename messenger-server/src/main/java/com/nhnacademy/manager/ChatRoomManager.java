package com.nhnacademy.manager;

import com.nhnacademy.model.ChatRoom;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatRoomManager {

    @Getter
    private static final ChatRoomManager instance = new ChatRoomManager();
    private final Map<String, ChatRoom> roomMaps = new ConcurrentHashMap<>();

    public ChatRoom createRoom(String name) {
        String id = UUID.randomUUID().toString();
        ChatRoom room = new ChatRoom(id, name);
        roomMaps.put(id, room);

        log.info("채팅방 생성 완료: id -> {}, name -> {}", id, name);
        return room;
    }

    public ChatRoom getRoom(String id) {
        return roomMaps.get(id);
    }

    public List<ChatRoom> getAllRooms() {
        return new ArrayList<>(roomMaps.values());
    }

    public void removeRoom(String id) {
        roomMaps.remove(id);
        log.info("채팅방 삭제 완료: id -> {}", id);
    }
}
