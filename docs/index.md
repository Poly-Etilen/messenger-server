# 메신저 애플리케이션 요구사항 문서

## 개요

본 문서는 Java와 TCP 소켓 기반의 클라이언트-서버 메신저 애플리케이션의 요구사항을 정의합니다.

## 프로젝트 구조
-  `Maven 멀티 모듈` 구성
- `messenger-common`: 공통 모듈 (메시지 모델, 유틸리티)
- `messenger-server`: 서버 애플리케이션 모듈
- `messenger-client`: 클라이언트 애플리케이션 모듈

---

## 셀프 체크리스트
- [셀프 체크리스트 확인하기](./self-check.md)

---

## 기술 스택

- Java 21

- Jackson JSON 라이브러리
  - https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
  - https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
  
- TCP 소켓 통신

## 주요 기능

### 기본 기능
- [사용자 로그인/로그아웃](./message-api-spec.md#로그인-관련)
- [사용자 목록 조회](./message-api-spec.md#사용자-관련)
- [채팅방 생성 및 입장](./message-api-spec.md#채팅-기능)
- [채팅방 목록 조회](./message-api-spec.md#채팅-기능)
- [채팅방 내 사용자 목록 조회](./message-api-spec.md#채팅-기능)
- [채팅 메시지 전송](./message-api-spec.md#채팅-기능)
- [귓속말 전송](./message-api-spec.md#채팅-기능)

### 추가 기능
- [추가 구현 1 - 파일 전송](./추가구현1.md)
- [추가 구현 2 - 실시간 알림 (Push)](./추가구현2.md)
- [추가 구현 3 - 성능 및 확장성 개선 (Queue 활용)](./추가구현3.md)
- [추가 구현 4 - 아키텍처 개선 (Design Patterns)](./추가구현4.md)
- [추가 구현 5 - NIO 기반 커넥션 관리](./추가구현5.md)

---

## API 명세서
- [기본 메시지 API 명세서](./message-api-spec.md)
- **추가 구현 명세서**
  - [추가 구현 1 - 파일 전송](./추가구현1.md)
  - [추가 구현 2 - 실시간 알림](./추가구현2.md)
  - [추가 구현 3 - 성능 및 확장성](./추가구현3.md)
  - [추가 구현 4 - 아키텍처 개선](./추가구현4.md)
  - [추가 구현 5 - NIO 커넥션 관리](./추가구현5.md)

---

## 비기능적 요구사항

- **보안**:  인증된사용자만 접속 허용
- **파일 전송**: 최대 파일 크기 제한 (10MB)

---

## 테스트

- JUnit 기반 단위 테스트

---

## 확장 가능성 고려 사항

- 추가 메시지 타입 대응 방안

---

## 배포

- 서버: 독립 실행형 JAR 배포
  - shade-plugin 활용한 패키징
  - https://maven.apache.org/plugins/maven-shade-plugin/

---