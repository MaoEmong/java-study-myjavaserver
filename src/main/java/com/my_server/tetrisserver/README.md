# Tetris Ranking Server

테트리스 게임의 점수와 랭킹을 관리하는 TCP 서버입니다.

## 개요

이 프로젝트는 테트리스 게임의 점수를 데이터베이스에 저장하고, 상위 랭킹을 조회할 수 있는 서버 애플리케이션입니다. TCP 소켓을 통해 클라이언트와 통신하며, JSON 형식의 메시지를 주고받습니다.

## 주요 기능

- **점수 제출**: 플레이어의 이름과 점수를 데이터베이스에 저장
- **랭킹 조회**: 상위 N명의 랭킹 정보 조회 (기본 10명, 최대 100명)
- **연결 확인**: ping/pong을 통한 서버 상태 확인

## 프로젝트 구조

```
tetrisserver/
├── DBConnection.java          # 데이터베이스 연결 관리
├── RankDto.java               # 랭킹 데이터 전송 객체 (Record)
├── TetrisRankRepository.java  # 데이터베이스 CRUD 작업
└── TetrisTcpServer.java      # TCP 서버 메인 클래스
```

## 클래스 설명

### DBConnection
- **역할**: MySQL 데이터베이스 연결을 제공하는 유틸리티 클래스
- **메서드**:
  - `getConnection()`: 데이터베이스 연결 객체를 반환

### RankDto
- **역할**: 랭킹 정보를 담는 데이터 전송 객체 (Java Record)
- **필드**:
  - `name`: 플레이어 이름
  - `score`: 점수

### TetrisRankRepository
- **역할**: 테트리스 점수 데이터베이스 작업을 담당하는 리포지토리
- **메서드**:
  - `insert(String name, int score)`: 새로운 점수 기록 추가
  - `findTop(int limit)`: 상위 N명의 랭킹 조회

### TetrisTcpServer
- **역할**: TCP 서버의 메인 클래스
- **기능**:
  - 포트 8000에서 클라이언트 연결 대기
  - 멀티스레드 방식으로 여러 클라이언트 동시 처리
  - JSON 형식의 요청/응답 처리

## 데이터베이스 설정

### 필수 요구사항
- MySQL 데이터베이스
- 데이터베이스 이름: `store`
- 테이블: `tetris_score_tb`

### 테이블 스키마
```sql
CREATE TABLE tetris_score_tb (
    name VARCHAR(255),
    score INT
);
```

## API 프로토콜

서버는 TCP 소켓을 통해 JSON 형식의 메시지를 주고받습니다.

### 1. Ping 요청
**요청**:
```json
{"type":"ping"}
```

**응답**:
```json
{"ok":true,"pong":true}
```

### 2. 점수 제출 (Submit)
**요청**:
```json
{"type":"submit","nickname":"플레이어이름","score":1000}
```

**응답 (성공)**:
```json
{"ok":true}
```

**응답 (실패)**:
```json
{"ok":false,"error":"bad_request"}
```
또는
```json
{"ok":false,"error":"db_fail"}
```

### 3. 랭킹 조회 (Get)
**요청**:
```json
{"type":"get","top":10}
```

**응답 (성공)**:
```json
{
  "ok":true,
  "rankings":[
    {"name":"플레이어1","score":5000},
    {"name":"플레이어2","score":4000},
    {"name":"플레이어3","score":3000}
  ]
}
```

**응답 (실패)**:
```json
{"ok":false,"error":"unknown_type"}
```

## 의존성

- **MySQL Connector**: `mysql:mysql-connector-java:8.0.33`
- **Java**: JDK 14 이상 (Record 타입 사용)

## 주의사항 

1. **JSON 파서**: 현재 구현된 JSON 파서는 매우 단순하며, 중첩된 객체나 배열을 지원하지 않습니다. 복잡한 JSON 처리가 필요한 경우 Jackson이나 Gson 같은 라이브러리를 사용하는 것을 권장합니다.

2. **에러 처리**: 일부 예외 상황에서 적절한 에러 응답을 반환하지 않을 수 있습니다.

3. **동시성**: 멀티스레드 환경에서 안전하게 동작하지만, 데이터베이스 연결 풀링을 고려하는 것이 좋습니다.
