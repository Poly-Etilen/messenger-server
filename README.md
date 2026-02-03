# 단계별 분류

## 1. Maven - 프로젝트 구성

- [X]  멀티 모듈 구조 생성
    - [X]  messenger-requirements
    - [X]  messenger-common
    - [X]  messenger-server
    - [X]  messenger-client
- [X]  공통 라이브러리 의존성 추가
    - [X]  Jackson-databind
    - [X]  Jackson-datatype-jsr310
- [X]  프로토콜 정의 구현
    - [X]  Length-Prefix 방식 구현(message-length: [길이]\n + JSON Payload)
        - MessageCodec.class

## 2. 로그인

- [X]  [Client] 로그인 요청 전송 (LOGIN) ← userId, password 포함
- [X]  [Server] 로그인 인증 로직 ← 계정 검증 및 SessionID 발급과 세션 맵에 저장
- [X]  [Client] 응답 처리 ← 성공 시 sessionId 저장 / 실패 시 에러 메시지 출력

## 3. Console 기반 Client 구현

- [X]  스레드 분리
    - Main Thread: 사용자 콘솔 입력 처리
    - Receiver Thread: 서버 메시지 수신 및 로그 출력
- [ ]  명령어 처리기 구현
    - /login, /exit 등 텍스트 명령어를 프로토콜 메시지로 변환

## 4. GUI 기반 Client 구현 (Swing or JavaFX)

- [X]  로그인 화면 구현 ← ID/PW 입력 필드 및 로그인 버튼
- [X]  대기실 화면 구현 ← 로그아웃 버튼, 사용자 목록 표시 영역, 채팅방 목록 표시 영역
- [X]  채팅방 화면 구현 ← 메시지 입력창, 전송 버튼, 대화 내용 출력 영역
- [X]  UI-네트워크 연동 ← 서버 응답 수신시 JavaFX 또는 Swing으로 UI 업데이트

## 5. 로그아웃

- [X]  [Client] 로그아웃 요청 ← 헤더에 sessionId 포함하여 전송
- [X]  [Server] 세션 정리 ← 세션 저장소에서 해당 사용자 제거 및 소켓 연결 종료

## 6. 사용자 목록 요청

- [X]  [Client] 사용자 목록 요청 (USER-LIST)
- [X]  [Server] 접속자 목록 반환 ← userId, name, online 상태 포함된 리스트 전송

## 7. 채팅방 생성/입장

- [X]  [Client] 채팅방 생성 요청
- [X]  [Server] 채팅방 생성 및 ID 발급 ← 고유 roomId 생성 및 방 목록에 추가
- [X]  [Client] 채팅방 입장 요청
- [X]  [Server] 입장 처리 ← 해당 방의 참여자 목록에 사용자 추가

## 8. 채팅방 목록/조회

- [X]  [Client] 채팅방 목록 요청
- [X]  [Server] 전체 방 목록 반환 ← roomId, roomName, userCount 정보 전송

## 9. 채팅 메시지 전송

- [X]  [Client] 메시지 전송 ← roomId, message 내용 포함
- [X]  [Server] 브로드캐스트
    - 해당 방에 있는 모든 사용자에게 메시지 전달
    - 발신자에게 messageId 포함된 성공 응답 전송

## 10. 귓속말

- [ ]  [Client] 귓속말 전송 ← receiverId 지정하여 메시지 전송
- [X]  [Server] 1:1 메시지 전달 ← 수신자 세션이 존재하는지 확인 후 특정 소켓으로만 전송
