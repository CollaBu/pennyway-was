## Domain 모듈

### 🤝 Rule

- 서비스 비지니스를 모른다.
- 하나의 모듈은 최대 하나의 Infrastructure에 대한 책임만을 갖거나 가지지 않는다.
- 도메인 모듈을 조합한 더 큰 단위의 도메인 모듈이 존재할 수 있다.
- Web 라이브러리 의존성을 갖는 것은 허용하지 않는다.
- Domain
    - Java Class로 표현된 도메인 Class들
- Repository
    - 도메인 조회, 저장, 수정, 삭제
    - 시스템에서 가장 보호받아야 하고 견고해야 한다.
    - 구현하려는 기능이 중심 역할이라면 도메인 모듈, 아니라면 사용하는 측에서 작성하도록 만드는 것이 좋다.
- Domain Service
    - Domain의 비지니스 책임
    - Domain의 비지니스가 단순하면 생기지 않을 수도 있다.
    - 트랜잭션의 단위, 요청 데이터 검증, 이벤트 발생 등의 비지니스로 사용
    - Domain 모듈의 Service는 @DomainService를 사용한다.

### 🏷️ Directory Structure

```agsl
pennyway-common
├── src
│   ├── main
│   │   ├── java.kr.co.pennyway.domain
│   │   │   ├── domains # 도메인 별로 패키지를 나누어 구성한다.
│   │   │   │   ├── entity
│   │   │   │   │   ├── domain
│   │   │   │   │   ├── exception
│   │   │   │   │   ├── repository
│   │   │   │   │   ├── service
│   │   │   │   │   └── type
│   │   │   │   └── …
│   │   │   ├── common
│   │   │   │   ├── redis # Redis Entity, Repository, Service
│   │   │   │   │   └── … 
│   │   │   │   └── …
│   │   │   ├── config
│   │   │   └── DomainPackageLocation.java
│   │   └── resources
│   │       └── application-domain.yml
│   └── test
├── build.gradle
├── README.md
└── settings.gradle
```