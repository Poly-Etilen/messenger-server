# 셀프 체크리스트 (Self-Checklist)

본 문서는 메신저 시스템 개발 완료 후 요구사항 충족 여부를 스스로 확인하기 위한 체크리스트입니다.

## 1. 프로젝트 기본 구성 및 기술 스택
- [X] Maven 멀티 모듈 구조로 구성되어 있는가? (`messenger-common`, `messenger-server`, `messenger-client`)
- [X] Java 21 버전을 사용하는가?
- [X] Jackson 라이브러리를 사용하여 JSON 직렬화/역직렬화를 처리하는가?
- [X] TCP 소켓 통신을 기반으로 구현되었는가?

## 2. 공통 규격 및 비기능적 요소
- [X] **메시지 규격**
    - [X] Text-based Length-Prefix 방식을 준수하는가? (`message-length: [길이]\n{JSON}`)
    - [X] 모든 요청의 헤더에 `sessionId`가 포함되어 있으며, 서버에서 이를 검증하는가?
- [X] **예외 처리**
    - [X] 모든 실패 응답이 공통 에러 형식(`code`, `message`)을 따르는가?
- [X] **보안**
    - [X] 인증된 사용자만 시스템 이용이 가능한가?

## 3. 기본 기능 구현 (Basic Features)
- [X] **로그인 및 사용자 관리**
    - [X] `LOGIN`: 로그인 요청 및 성공(sessionId 발급)/실패 응답이 정상적인가?
    - [X] `LOGOUT`: 로그아웃 요청 및 세션 종료 처리가 정상적인가?
    - [X] `USER-LIST`: 전체 사용자 목록 및 온라인 상태 조회가 가능한가?
- [X] **채팅방 관리**
    - [X] `CHAT-ROOM-CREATE`: 채팅방 생성이 정상적으로 작동하는가?
    - [X] `CHAT-ROOM-LIST`: 채팅방 목록 조회가 정상적으로 작동하는가?
    - [X] `CHAT-ROOM-ENTER`: 채팅방 입장 및 현재 참여자 목록 반환이 정상적인가?
    - [X] `CHAT-ROOM-EXIT`: 채팅방 나가기 처리가 정상적인가?
- [X] **메시징 기능**
    - [X] `CHAT-MESSAGE`: 채팅방 내 메시지 전송 및 브로드캐스트가 정상적인가?
    - [X] `PRIVATE-MESSAGE`: 특정 사용자에 대한 귓속말 전송이 가능한가?
    - [X] `CHAT-MESSAGE-HISTORY`: 과거 메시지 기록 조회가 가능한가?
    - [X] 메시지 전송 성공 시 고유한 `messageId`를 반환하는가?

## 4. 추가 구현 사항 (Advanced Features)
- [X] **추가 구현 1: 파일 전송**
    - [X] `FILE-TRANSFER`: Base64 인코딩을 통한 파일 전송이 구현되었는가?
    - [X] 파일 크기 제한(10MB) 초과 시 에러 처리를 하는가?
- [X] **추가 구현 2: 실시간 알림 (Push)**
    - [X] `PUSH-NEW-MESSAGE`: 새 메시지 수신 시 실시간 알림을 보내는가?
    - [X] `PUSH-ROOM-ENTER` / `PUSH-ROOM-EXIT`: 사용자 입장/퇴장 알림이 전달되는가?
- [X] **추가 구현 3: 성능 개선 (Queue)**
    - [X] 메시지 저장 및 브로드캐스트 처리를 비동기(Queue 활용)로 처리하는가?
    - [X] Push 알림 발송 로직이 메인 메시징 로직과 분리되어 있는가?
- [X] **추가 구현 4: 아키텍처 개선 (Design Patterns)**
    - [X] **Observer 패턴**: UI와 메시지 수신 로직이 분리되어 있는가?
    - [X] **Command 패턴**: 채팅 명령어(/list, /exit 등) 처리가 객체화되어 있는가?
    - [X] **Factory Method 패턴**: 메시지 타입별 객체 생성 로직이 캡슐화되어 있는가?
    - [X] **Strategy 패턴**: 메시지 타입별 처리 알고리즘이 동적으로 교체 가능한가?
- [X] **추가 구현 5: NIO 기반 커넥션 관리**
    - [X] Java NIO를 사용하여 Non-blocking I/O 모델로 구현되었는가?
    - [X] Selector를 활용하여 대규모 동시 접속을 효율적으로 관리하는가?

## 5. 테스트 및 배포
- [X] JUnit 기반의 단위 테스트가 작성되어 있는가?
- [X] `maven-shade-plugin`을 사용하여 독립 실행형 JAR로 빌드가 가능한가?
