## 🔌 Socket 모듈

### 🎯 핵심 역할

- 실시간 양방향 통신 제공
- STOMP 기반 WebSocket 처리
- 분산 환경 고려한 설계

### ⚡ 주요 특징

- Redis Pub/Sub 활용한 세션 관리
- 무상태(Stateless) 설계로 수평 확장 용이
- 실시간 이벤트 처리에 최적화

### 🏷️ Directory Structure

> 명확히 정해진 규칙은 없지만, 이유가 없다면 아래 규칙을 따른다.

```
pennyway-socket
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   ├── socket
│   │   │   │   ├── controller
│   │   │   │   ├── service
│   │   │   │   ├── relay # 추후 분리 용이성을 위해 구분
│   │   │   │   ├── common
│   │   │   │   ├── config
│   │   │   │   └── PennywaySocketApplication.java
│   │   └── resources
│   │       └── application.yml
│   └── test
├── build.gradle
├── README.md
└── Dockerfile
```