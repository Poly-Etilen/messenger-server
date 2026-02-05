package com.nhnacademy;

import com.nhnacademy.command.impl.LoginCommand;
import com.nhnacademy.command.impl.LogoutCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.model.ChatRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.mockito.Mockito.*;

public class AuthTest extends ServerTestSupport {

    @Test
    @DisplayName("로그인 성공: 세션 매니저에 등록되고 UserID가 설정되어야 함")
    void loginSuccessTest() {
        LoginCommand command = new LoginCommand();
        injectDependencies(command);

        when(userRepository.authenticate("marco", "nhnacademy123")).thenReturn(true);
        // 로그인 중복 체크 통과를 위해 false 반환 설정 (기본값이지만 명시적으로 표현)
        when(sessionManager.isLoggedIn("marco")).thenReturn(false);

        Message request = createMessage(MessageType.LOGIN, Map.of(
                "userId", "marco",
                "password", "nhnacademy123"
        ));

        command.execute(request);

        verify(sessionManager).addSession("marco", session);
        verify(session).setUserId("marco");

        // 로그인 성공 메시지 전송 확인
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(observer).sendMessage(messageCaptor.capture());
        Assertions.assertEquals(MessageType.LOGIN_SUCCESS, messageCaptor.getValue().getHeader().getMessageType());
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 틀림")
    void loginFailTest() {
        LoginCommand command = new LoginCommand();
        injectDependencies(command);

        // 인증 실패 설정
        when(userRepository.authenticate("marco", "wrongPassword")).thenReturn(false);

        Message request = createMessage(MessageType.LOGIN, Map.of(
                "userId", "marco",
                "password", "wrongPassword"
        ));

        command.execute(request);

        // 세션 매니저에 등록되지 않았음을 검증
        verify(sessionManager, never()).addSession(anyString(), any());

        // 실패 메시지 전송 확인
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(observer).sendMessage(messageCaptor.capture());
        Assertions.assertEquals(MessageType.LOGIN_FAIL, messageCaptor.getValue().getHeader().getMessageType());
    }

    @Test
    @DisplayName("로그아웃: 세션 제거 및 현재 방에서 퇴장 처리")
    void logoutTest() {
        // 1. 세션 사용자 ID 설정 (Stubbing)
        when(session.getUserId()).thenReturn("marco");
        when(observer.getUserId()).thenReturn("marco");
        when(sessionManager.isLoggedIn("marco")).thenReturn(true);

        // 2. 채팅방 설정
        ChatRoom room = new ChatRoom("room-id", "Existing Room");
        when(chatRoomManager.getRoom(anyString())).thenReturn(room);

        // 3. 현재 세션이 방에 들어가 있는 상태 조성
        room.addSession(session.getObserver());

        when(session.getCurrentRoomId()).thenReturn(room.getId());

        LogoutCommand command = new LogoutCommand();
        injectDependencies(command);

        command.execute(createMessage(MessageType.LOGOUT, Map.of()));

        // 1. 세션 매니저에서 제거되었는지 검증
        verify(sessionManager).removeSession("marco");

        // 2. 방에서 실제로 나갔는지 확인 (ChatRoom은 실제 객체이므로 상태 확인 가능)
        Assertions.assertFalse(room.getSessions().contains(session.getObserver()), "채팅방 목록에서 세션이 제거되어야 합니다.");
        verify(session).setCurrentRoomId(null);
    }
}