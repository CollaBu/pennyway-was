## Pennyway Domain Modules

### 🏛️ Architecture Overview

<div align="center">
  <img src="https://github.com/user-attachments/assets/54694ba6-b8e0-47dd-9eae-2e94fdd9ccd4" width="600">
</div>

도메인 모듈은 세 가지 주요 컴포넌트로 구성된다.

- `domain-service`: 핵심 비즈니스 로직 및 도메인 간 조율
- `domain-rdb`: MySQL/JPA 관련 구현
- `domain-redis`: Redis 관련 구현

## 🤝 Convention & Rules

### 공통 규칙

- 웹 관련 라이브러리 의존성 금지
- 외부 시스템과의 직접적인 통신 금지
- 각 모듈은 자신의 책임에 집중

### Domain Service Module

- 핵심 비즈니스 로직 구현
- 여러 도메인/저장소 간 상호작용 조율
- `@DomainService` 어노테이션 사용

### Infrastructure Modules (RDB/Redis)

- 단일 저장소 책임 원칙
- 저장소 특화 기능 구현
- 기본적인 유효성 검증 및 데이터 접근 로직, 불변식 검증
- 하나의 모듈은 최대 하나의 Infrastructure에 대한 책임만을 갖거나 가지지 않는다.
- 도메인 모듈을 조합한 더 큰 단위의 도메인 모듈이 존재할 수 있다.

### 🏷️ Directory Structure

```
pennyway-domain/
├── domain-service/
│   ├── src/main/java/kr/co/pennyway/domain/
│   │   ├── common/
│   │   ├── config/
│   │   └── context/          # 도메인 컨텍스트별 구성
│   │       ├── chat/
│   │       ├── account/
│   │       └── finance/
│   └── resources/
│       └── application-domain-service.yml
│
├── domain-rdb/
│   ├── src/main/java/kr/co/pennyway/domain/
│   │   ├── common/
│   │   ├── config/
│   │   └── domains/         # Entity 기반 구성
│   │       ├── user/
│   │       └── chat/
│   └── resources/
│       └── application-domain-rdb.yml
│
└── domain-redis/
├── src/main/java/kr/co/pennyway/domain/
│   ├── common/
│   ├── config/
│   └── domains/        # Redis 모델 기반 구성
│       ├── session/
│       └── cache/
└── resources/
└── application-domain-redis.yml
```

## 🎯 책임 분리 가이드

### Domain Service가 담당하는 것

- 복잡한 비즈니스 규칙
- 다중 도메인 간 조율
- 트랜잭션 관리

### Infrastructure Service가 담당하는 것

- 단순 CRUD 연산
- 저장소 특화 기능 (캐싱, 락 등)
- 기본적인 데이터 검증

## 💡 Tips

- 새로운 기능 개발 시 도메인 책임 소재 먼저 파악하기
- 단순 CRUD는 인프라 모듈에서 처리
- 복잡한 비즈니스 로직은 domain-service로
- 테스트는 각 모듈의 책임에 맞게 작성