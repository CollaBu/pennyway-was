## Common 모듈

### 🤝 Rule

- 하나의 프로젝트에서 모든 모듈에서 사용될 수밖에 없는 것들
- Type, Util 등을 정의한다.
- 가능하면 사용하지 않는다.
    - common 모듈에 기능을 추가할 때는 팀원과 상의한다.
- 프로젝트 내 어떠한 모듈도 의존해서는 안 된다.
    - 최대한 오픈 소스로 배포 가능한 수준을 유지한다.

### 🏷️ Directory Structure

```agsl
pennyway-common
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway
│   │   │   └── common # 공통으로 사용되는 Type, Util 등을 기능 별로 정의한다.
│   │   └── resources
│   │       └── application-common.yml
│   └── test
├── build.gradle
├── README.md
└── settings.gradle
```