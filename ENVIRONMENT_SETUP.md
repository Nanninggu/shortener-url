# 환경별 실행 가이드

## 📋 환경 구분

### Local 환경 (로컬 개발)
- **목적**: 로컬 머신에서 개발 및 테스트
- **프론트엔드**: `http://localhost:3000`
- **백엔드**: `http://localhost:8080`
- **프로파일**: 기본 (application.yml)

### Prod 환경 (NCP VM)
- **목적**: NCP VM에서 프로덕션 서비스 운영
- **프론트엔드**: `http://223.130.157.227:3000`
- **백엔드**: `http://223.130.157.227:8080`
- **프로파일**: `prod` (application-prod.yml)

---

## 🖥️ Local 환경 실행

### 백엔드 실행

```bash
cd springboot
mvn spring-boot:run
```

또는 JAR 파일로 실행:
```bash
cd springboot
mvn clean package -DskipTests
java -jar target/url-shortener-1.0.0.jar
```

**접속**: `http://localhost:8080`

### 프론트엔드 실행

```bash
cd vue
npm install
npm run dev
```

**접속**: `http://localhost:3000`

**특징**:
- Vite 프록시가 `/api` 요청을 `http://localhost:8080`으로 자동 전달
- 환경 변수 불필요 (기본값 사용)

---

## ☁️ Prod 환경 실행 (NCP VM)

### 백엔드 실행

**방법 1: systemd 서비스 사용 (권장)**

```bash
# 서비스 파일 복사
sudo cp springboot/url-shortener.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable url-shortener
sudo systemctl start url-shortener

# 상태 확인
sudo systemctl status url-shortener
```

**방법 2: 직접 실행**

```bash
cd /opt/url-shortener/springboot
export SPRING_PROFILES_ACTIVE=prod
export APP_BASE_URL=http://223.130.157.227:8080
java -jar target/url-shortener-1.0.0.jar
```

**접속**: `http://223.130.157.227:8080`

### 프론트엔드 실행

**방법 1: 개발 모드 (포트 3000 직접 사용)**

```bash
cd /opt/url-shortener/vue
npm install
npm run dev -- --host 0.0.0.0 --port 3000
```

**방법 2: 프로덕션 빌드 후 실행**

```bash
cd /opt/url-shortener/vue

# 환경 변수 설정
export VITE_API_BASE_URL=http://223.130.157.227:8080/api

# 빌드
npm run build

# 정적 파일 서빙 (간단한 HTTP 서버)
cd dist
python3 -m http.server 3000
```

**방법 3: PM2 사용 (권장)**

```bash
# PM2 설치
sudo npm install -g pm2

# 개발 모드로 실행
cd /opt/url-shortener/vue
pm2 start npm --name "url-shortener-frontend" -- run dev -- --host 0.0.0.0 --port 3000

# 또는 프로덕션 빌드 후
cd /opt/url-shortener/vue
export VITE_API_BASE_URL=http://223.130.157.227:8080/api
npm run build
cd dist
pm2 serve . 3000 --name "url-shortener-frontend" --spa
```

**접속**: `http://223.130.157.227:3000`

---

## 🔧 환경 변수

### Local 환경
- 환경 변수 불필요
- 프록시 자동 처리

### Prod 환경

**백엔드 (systemd 서비스)**
```ini
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="APP_BASE_URL=http://223.130.157.227:8080"
```

**프론트엔드**
```bash
export VITE_API_BASE_URL=http://223.130.157.227:8080/api
```

또는 `.env.production` 파일:
```
VITE_API_BASE_URL=http://223.130.157.227:8080/api
```

---

## ✅ 설정 확인

### Local 환경 확인
```bash
# 백엔드
curl http://localhost:8080/actuator/health

# 프론트엔드
curl http://localhost:3000
```

### Prod 환경 확인
```bash
# 백엔드
curl http://223.130.157.227:8080/actuator/health

# 프론트엔드
curl http://223.130.157.227:3000
```

---

## 🔄 환경 전환

### Local → Prod
1. `SPRING_PROFILES_ACTIVE=prod` 설정
2. `APP_BASE_URL` 환경 변수 설정
3. 프론트엔드 `VITE_API_BASE_URL` 환경 변수 설정
4. CORS 설정 확인 (자동으로 포함됨)

### Prod → Local
1. `SPRING_PROFILES_ACTIVE` 제거 또는 기본값 사용
2. 환경 변수 제거
3. 기본 설정 사용

---

## 📝 주요 차이점

| 항목 | Local | Prod |
|------|-------|------|
| 프로파일 | 기본 | `prod` |
| Base URL | `http://localhost:8080` | `http://223.130.157.227:8080` |
| 프론트엔드 API | `/api` (프록시) | `http://223.130.157.227:8080/api` |
| CORS | localhost만 | localhost + NCP VM IP |
| 로그 레벨 | DEBUG | INFO |
| 데이터베이스 | Embedded H2 | Embedded H2 (TCP 모드) |

---

**자세한 배포 가이드**: `NCP_DEPLOYMENT_GUIDE.md` 참조

