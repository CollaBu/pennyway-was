## 📱 External-API 모듈

### 🎯 핵심 역할

- 외부 클라이언트와의 통신 담당
- RESTful API 엔드포인트 제공
- 인증/인가 처리
- 비즈니스 유스케이스 조율

### 🔗 의존성 규칙

- domain-service 모듈 의존
- 필요에 따라 infra/redis/rdb 모듈 직접 사용 가능
- Spring Web, Security 의존성 포함

### 📌 Architecture

<div align="center">
  <img src="https://github.com/CollaBu/pennyway-was/assets/96044622/932db02d-7c08-4052-82d5-014148f035a0" width="600">
</div>

- Facade 패턴을 사용하여 Controller와 Service 계층을 분리하여 단위 테스트를 용이하게 한다.
- Controller -> UseCase -> Domain Service 흐름으로 진행한다.
    1. HTTP 요청/응답 처리 (Controller)
    2. 비즈니스 흐름 조율 (UseCase)
    3. 도메인 로직을 호출하여, 인프라 통합 서비스 비즈니스 구현 (Domain Service)
        - 기능이 너무 단순하면 없을 수도 있다.

### 🏷️ Directory Structure

```
pennyway-app-external-api
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   ├── api
│   │   │   │   ├── apis
│   │   │   │   │   ├── auth # 기능 관심사 별로 패키지를 나누어 구성한다.
│   │   │   │   │   │   ├── api
│   │   │   │   │   │   ├── controller
│   │   │   │   │   │   ├── dto
│   │   │   │   │   │   ├── usecase
│   │   │   │   │   │   └── …
│   │   │   │   │   └── …
│   │   │   │   ├── common
│   │   │   │   └── config
│   │   │   └── PennywayExternalApiApplication.java
│   │   └── resources
│   │       └── application.yml
│   └── test
├── build.gradle
├── README.md
└── Dockerfile
```