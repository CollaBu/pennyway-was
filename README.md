## 💰 Pennyway
> 지출 관리 SNS 플랫폼

| Version # | Revision Date | Description   | Author |
|:---------:|:-------------:|:--------------|:------:|
|  v0.0.1   |  2024.03.07   | 프로젝트 기본 설명 작성 | 양재서 |

<br/>

## 👪 Backend Team

<table>
    <tr>
        <td align="center">
            <a href="https://github.com/psychology50">양재서</a>
        </td>
        <td align="center">
            <a href="https://github.com/jinlee1703">이진우</a>
        </td>
        <td align="center">
            <a href="https://github.com/asn6878">안성윤</a>
        </td>
    </tr>
    <tr>
        <td align="center">
            <a href="https://github.com/psychology50"><img height="200px" width="200px" src="https://avatars.githubusercontent.com/u/96044622?v=4"/></a>
        </td>
        <td align="center">
            <a href="https://github.com/jinlee1703"><img height="200px" width="200px" src="https://avatars.githubusercontent.com/u/68031450?v=4"/></a>
        </td>
        <td align="center">
            <a href="https://github.com/asn6878"><img height="200px" width="200px" src="https://avatars.githubusercontent.com/u/79460319?v=4"/></a>
        </td>
    </tr>
</table>


<br/>

## 🌳 Branch Convention
> 💡 Git-Flow 전략을 사용합니다.
- main
    - 배포 가능한 상태의 코드만을 관리하는 프로덕션용 브랜치
    - PM(양재서)의 승인 후 병합 가능
- dev
    - 개발 전용 브랜치
    - 한 명 이상의 팀원의 승인 후 병합 가능
    - 기능 개발이 완료된 브랜치를 병합하여 테스트를 진행
- 이슈 기반 브랜치
    - 이슈는 `{티켓번호}-{브랜치명}`을 포함한다.
    - `feat/{티켓번호}-{브랜치명}`: 신규 기능 개발 시 브랜치명
    - `fix/{티켓번호}-{브랜치명}`: 리팩토링, 수정 작업 시 브랜치명
    - `hotfix/{티켓번호}-{브랜치명}`: 빠르게 수정해야 하는 버그 조치 시 브랜치명

<br/>

## 🤝 Commit Convention
> 💡 angular commit convention
- feat: 신규 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- rename: 주석, 로그, 변수명 등 수정
- style: 코드 포맷팅, 세미콜론 누락 (코드 변경 없는 경우)
- refactor: 코드 리팩토링
- test: 테스트 코드, 리펙토링 테스트 코드 추가
- chore: 빌드 업무 수정, 패키지 매니저 수정

<br/>

## 📌 Architecture
### 1️⃣ System Architecture

<div align="center">
  <img src="https://github.com/KCY-Fit-a-Pet/fit-a-pet-server/assets/96044622/d2453fe7-ed90-4028-be5b-22fe1b2f3f44" width="600">
</div>

### 2️⃣ Infrastructure Architecture

<div align="center">
  <img src="https://github.com/KCY-Fit-a-Pet/fit-a-pet-server/assets/96044622/f7cf3405-c112-4531-b313-9e3fcb10f673" width="600">
</div>

### 3️⃣ Multi Module Architecture

<div align="center">
  <img src="https://github.com/KCY-Fit-a-Pet/fit-a-pet-server/assets/96044622/b6a47354-f314-4d09-ba17-116fe86b64cf" width="600">
</div>

### 4️⃣ ERD

<div align="center">
  <img src="" width="600">
</div>

<br/>

## 📗 Tech Stack
### 1️⃣ Framework & Library
- JDK 17
- SpringBoot 3.2.3
- SpringBoot Security 6.2.2
- Spring Data JPA 3.2.3
- Spring Doc Open API 2.3.0
- Lombok 1.18.30
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- jjwt 0.11.5
- httpclient5 5.2.25.RELEASE
- OpenFeign 4.0.6

### 2️⃣ Infrastructure Architecture
- Gradle 7.6.4

### 3️⃣ Multi Module Architecture
- MySQL 8
- Redis 7.0

### 4️⃣ ERD
- AWS EC2 (for Build Server)
- AWS GW
- AWS S3
- Docker & Docker-compose
- Ngnix
- GitHub Actions
