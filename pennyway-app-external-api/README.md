## External-API 모듈

### 🤝 Rule

- batch, worker, internal-api, external-api 등의 모듈과 묶일 수 있다.
- 사용성에 따라 다른 모든 계층에 의존성을 추가하여 사용할 수 있다.
- 웹 및 security 관련 라이브러리 의존성을 갖는다.
- Presentation Layer에 해당하는 Controller와 핵심 비즈니스 로직을 처리하는 Usecase를 포함한다.

### 📌 Architecture

<div align="center">
  <img src="https://github.com/CollaBu/pennyway-was/assets/96044622/932db02d-7c08-4052-82d5-014148f035a0" width="600">
</div>

- Facade 패턴을 사용하여 Controller와 Service 계층을 분리하여 단위 테스트를 용이하게 한다.

### 🏷️ Directory Structure

```
pennyway-app-external-api
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   ├── api
│   │   │   │   ├── apis
│   │   │   │   │   ├── auth # 기능 관심사 별로 패키지를 나누어 구성한다.
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
└── settings.gradle
```