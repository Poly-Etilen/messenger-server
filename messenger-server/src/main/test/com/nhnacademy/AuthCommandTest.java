package com.nhnacademy;

import com.nhnacademy.command.impl.LoginCommand;
import com.nhnacademy.command.impl.LogoutCommand;
import com.nhnacademy.domain.Header.MessageType;
import com.nhnacademy.domain.Message;
import com.nhnacademy.manager.ChatRoomManager;
import com.nhnacademy.manager.SessionManager;
import com.nhnacademy.model.ChatRoom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AuthCommandTest extends ServerTestSupport{

    @Test
    @DisplayName("로그인 성공: 세션 메니저 등록 및 UserID 설정 확인")
    void loginSuccessTest(){
        LoginCommand loginCommand = new LoginCommand();
        Message request = createMessage(MessageType.LOGIN, Map.of(
                "userId", "marco",
                "password", "nhnacademy123"
        ));

        loginCommand.execute(session, request);

        Assertions.assertTrue(SessionManager.getInstance().isLoggedIn("marco"));
        Assertions.assertEquals("marco", session.getUserId());
        Assertions.assertTrue(out.size() > 0, "성공 응답이 전송되어야 함.");
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 불일치")
    void loginFailTest(){
        LoginCommand loginCommand = new LoginCommand();
        Message request = createMessage(MessageType.LOGIN, Map.of(
                "userId", "marco",
                "password", "wrongPassword"
        ));
        loginCommand.execute(session, request);
        Assertions.assertFalse(SessionManager.getInstance().isLoggedIn("marco"));
        Assertions.assertNull(session.getUserId());
    }

    @Test
    @DisplayName("로그아웃: 세션 제거 및 방 퇴장 확인")
    void logoutTest(){
        session.setUserId("marco");
        SessionManager.getInstance().addSession("marco", session);

        ChatRoom room = ChatRoomManager.getInstance().createRoom("room1");
        room.addSession(session);
        session.setCurrentRoomId(room.getId());

        LogoutCommand logoutCommand = new LogoutCommand();
        Message request = createMessage(MessageType.LOGOUT, Map.of());

        logoutCommand.execute(session, request);

        Assertions.assertFalse(SessionManager.getInstance().isLoggedIn("marco"));
        Assertions.assertFalse(room.getSessions().contains(session));
        Assertions.assertNull(session.getCurrentRoomId());
    }
}
