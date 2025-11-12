# H-Link URL Shortener

URL 단축 서비스 애플리케이션입니다. Spring Boot 백엔드와 Vue.js 프론트엔드로 구성되어 있습니다.

## 기술 스택

### Backend
- Spring Boot 3.2.0
- MyBatis
- PostgreSQL
- Java 17

### Frontend
- Vue.js 3
- Vue Router
- Axios
- Vite

## 프로젝트 구조

```
shortener-url/
├── springboot/          # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hlink/urlshortener/
│   │   │   │   ├── controller/    # MVC Controller
│   │   │   │   ├── service/       # Business Logic
│   │   │   │   ├── mapper/        # MyBatis Mapper
│   │   │   │   ├── model/         # Entity
│   │   │   │   └── dto/           # Data Transfer Object
│   │   │   └── resources/
│   │   │       ├── mapper/        # MyBatis XML
│   │   │       └── db/            # Database Schema
│   │   └── pom.xml
└── vue/                 # Vue.js 프론트엔드
    ├── src/
    │   ├── views/      # 페이지 컴포넌트
    │   ├── services/   # API 서비스
    │   └── router/     # 라우터 설정
    └── package.json
```

## 데이터베이스 설정

1. PostgreSQL 설치 및 실행
2. 데이터베이스 생성:
```sql
CREATE DATABASE url_shortener;
```

3. 스키마 실행:
```bash
psql -U postgres -d url_shortener -f springboot/src/main/resources/db/schema.sql
```

4. `application.yml`에서 데이터베이스 연결 정보 수정:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/url_shortener
    username: your_username
    password: your_password
```

## 실행 방법

### Backend 실행

```bash
cd springboot
mvn spring-boot:run
```

백엔드는 `http://localhost:8080`에서 실행됩니다.

### Frontend 실행

```bash
cd vue
npm install
npm run dev
```

프론트엔드는 `http://localhost:3000`에서 실행됩니다.

## API 엔드포인트

### URL 생성
- **POST** `/api/urls`
- Request Body:
```json
{
  "originalUrl": "https://example.com/very/long/url",
  "expirationDays": 30  // 선택사항
}
```

### URL 정보 조회
- **GET** `/api/urls/{shortCode}`

### 모든 URL 목록 조회
- **GET** `/api/urls`

### 단축 URL 리다이렉트
- **GET** `/{shortCode}`

## 주요 기능

- ✅ URL 단축 생성
- ✅ 단축 URL로 원본 URL 리다이렉트
- ✅ 클릭 수 추적
- ✅ 만료일 설정 (선택사항)
- ✅ 단축 URL 목록 조회
- ✅ 모던한 UI/UX

## 디자인 패턴

이 프로젝트는 **MVC (Model-View-Controller)** 패턴을 따릅니다:

- **Model**: `Url` 엔티티 및 MyBatis Mapper
- **View**: Vue.js 컴포넌트
- **Controller**: Spring Boot REST Controller
- **Service**: 비즈니스 로직 계층

