package com.nhnacademy.event.handler;

import com.nhnacademy.event.listener.ClientEventListener;
import com.nhnacademy.request.RequestFactory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommandHandler {

    private ClientEventListener listener;
    private ClientEventHandler handler;

    public void handler(String commandLine){
        String[] parts = commandLine.split("\\s+", 3);
        String command = parts[0];

        switch (command) {
            case "/help":
            case "/도움말":
                listener.writeMessage("""
                        [System] 명령어 목록:
                        /whisper <아이디> <메시지>
                        /history
                        /exit
                        /logout
                        """);
                break;
            case "/whisper":
            case "/w":
            case "/귓":
                if (parts.length < 3) {
                    listener.writeMessage("[System] 사용법: /whisper <아이디> <메시지>");
                } else {
                    sendWhisperMessage(parts[1], parts[2]);
                }
                break;
            case "/history":
            case "/기록":
                handler.sendRequest(RequestFactory.chatHistoryRequest(handler.getRoomId()));
                break;
            case "/exit":
            case "/나가기":
                handler.onExitRoomClicked();
                break;
            case "/logout":
                handler.onExitRoomClicked();
                handler.onLogoutClicked();
                break;
            default:
                listener.writeMessage("[System] 알 수 없는 명령어입니다: " + command);
        }
    }

    public void sendWhisperMessage(String targetId, String trim) {
        handler.sendRequest(RequestFactory.whisperRequest(targetId,trim));
    }


}
