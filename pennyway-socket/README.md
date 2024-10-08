## Socket 모듈

### 🤝 Rule

- 사용성에 따라 모든 계층에 의존성을 추가하여 사용할 수 있다.
- STOMP 기반의 WebSocket 의존성을 갖는다.
- 언제나 분산 서버로 확장할 수 있는 구조를 가져야 함에 유의한다.

### 🏷️ Directory Structure

> 명확히 정해진 규칙은 없지만, 이유가 없다면 아래 규칙을 따른다.

```
pennyway-socket
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   ├── socket
│   │   │   │   ├── chat
│   │   │   │   │   ├── controller
│   │   │   │   │   ├── dto
│   │   │   │   │   ├── service
│   │   │   │   │   └── …
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