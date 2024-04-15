## Infra 모듈

### 🤝 Rule

- 저장소, 도메인 외 시스템에서 필요한 모듈들
- 핵심, 도메인 비지니스를 모른다.
- 전체적인 시스템 서포트를 위한 기능 모듈이 만들어질 수 있다.
- web, client, event-publisher 등을 처리할 때 사용한다.
- 외부 Actor와의 통신을 위한 설정 및 구현을 포함한다.

### 🏷️ Directory Structure

```agsl
pennyway-common
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   └── infra 
│   │   │       ├── client # 외부 API 연동을 위한 모듈
│   │   │       ├── common
│   │   │       ├── config
│   │   │       └── PennywayInfraApplication.java
│   │   └── resources
│   │       └── application-infra.yml
│   └── test
├── build.gradle
├── README.md
└── settings.gradle
```