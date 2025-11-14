# NCP VM 배포 가이드

## 📋 환경 정보

- **공인 IP**: 223.130.157.227
- **포트**: 3000 (프론트엔드), 8080 (백엔드)
- **OS**: Ubuntu (NCP VM)

---

## 🔧 Local vs Prod 환경 분리

### Local 환경 (로컬 개발)
- **프론트엔드**: `http://localhost:3000`
- **백엔드**: `http://localhost:8080`
- **프로파일**: 기본 (application.yml)
- **실행 방법**: 
  ```bash
  # 백엔드
  cd springboot && mvn spring-boot:run
  
  # 프론트엔드
  cd vue && npm run dev
  ```

### Prod 환경 (NCP VM)
- **프론트엔드**: `http://223.130.157.227:3000`
- **백엔드**: `http://223.130.157.227:8080`
- **프로파일**: `prod` (application-prod.yml)
- **실행 방법**: 아래 배포 가이드 참조

---

## 🚀 NCP VM 배포 방법

### 1. 로컬에서 프로젝트 준비

```bash
cd /Users/may9noy/Documents/shortener-url

# Git에 커밋 (선택사항)
git add .
git commit -m "NCP VM 배포 준비"
git push
```

### 2. NCP VM에 접속

```bash
ssh user@223.130.157.227
```

### 3. 필수 패키지 설치

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk maven nodejs npm git
```

### 4. 프로젝트 클론 또는 업로드

**방법 1: Git 사용 (권장)**
```bash
cd /tmp
git clone <your-repo-url> url-shortener
cd url-shortener
```

**방법 2: SCP로 파일 전송**
```bash
# 로컬에서 실행
scp -r springboot vue deploy.sh user@223.130.157.227:/tmp/url-shortener/
```

### 5. 배포 스크립트 실행

```bash
cd /tmp/url-shortener
chmod +x deploy.sh
sudo ./deploy.sh
```

---

## 📝 수동 배포 (배포 스크립트 사용 불가 시)

### 1. 디렉토리 생성

```bash
sudo mkdir -p /opt/url-shortener
sudo mkdir -p /opt/url-shortener/logs
sudo mkdir -p /opt/url-shortener/data
sudo useradd -r -s /bin/bash -d /opt/url-shortener urlshortener || true
sudo chown -R urlshortener:urlshortener /opt/url-shortener
```

### 2. 프로젝트 파일 복사

```bash
sudo cp -r springboot /opt/url-shortener/
sudo cp -r vue /opt/url-shortener/
sudo chown -R urlshortener:urlshortener /opt/url-shortener
```

### 3. 백엔드 빌드 및 실행

```bash
cd /opt/url-shortener/springboot

# 빌드
sudo -u urlshortener mvn clean package -DskipTests

# systemd 서비스 설정
sudo cp url-shortener.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable url-shortener
sudo systemctl start url-shortener

# 상태 확인
sudo systemctl status url-shortener
```

### 4. 프론트엔드 빌드 및 실행

**옵션 1: 개발 모드로 실행 (포트 3000 직접 사용)**
```bash
cd /opt/url-shortener/vue
sudo -u urlshortener npm install
sudo -u urlshortener npm run dev -- --host 0.0.0.0 --port 3000
```

**옵션 2: 프로덕션 빌드 후 정적 파일 서빙**
```bash
cd /opt/url-shortener/vue
sudo -u urlshortener npm install
sudo -u urlshortener npm run build

# Nginx로 서빙 (nginx.conf 사용)
# 또는 간단한 HTTP 서버 사용
cd dist
sudo -u urlshortener python3 -m http.server 3000
```

---

## ⚙️ 환경 변수 설정

### 백엔드 (systemd 서비스)

`/etc/systemd/system/url-shortener.service` 파일 수정:

```ini
[Service]
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="APP_BASE_URL=http://223.130.157.227:8080"
Environment="H2_PASSWORD=your_password_here"
Environment="JWT_SECRET=your_jwt_secret_here"
```

변경 후:
```bash
sudo systemctl daemon-reload
sudo systemctl restart url-shortener
```

### 프론트엔드 (환경 변수)

프로덕션 빌드 시 환경 변수 설정:

```bash
cd /opt/url-shortener/vue
export VITE_API_BASE_URL=http://223.130.157.227:8080/api
sudo -u urlshortener npm run build
```

또는 `.env.production` 파일 생성:

```bash
cd /opt/url-shortener/vue
echo "VITE_API_BASE_URL=http://223.130.157.227:8080/api" > .env.production
sudo -u urlshortener npm run build
```

---

## 🔍 서비스 관리

### 백엔드 서비스

```bash
# 상태 확인
sudo systemctl status url-shortener

# 시작
sudo systemctl start url-shortener

# 중지
sudo systemctl stop url-shortener

# 재시작
sudo systemctl restart url-shortener

# 로그 확인
sudo journalctl -u url-shortener -f
```

### 프론트엔드 (PM2 사용 권장)

PM2로 프론트엔드를 관리하려면:

```bash
# PM2 설치
sudo npm install -g pm2

# 프론트엔드 실행
cd /opt/url-shortener/vue
pm2 start npm --name "url-shortener-frontend" -- run dev -- --host 0.0.0.0 --port 3000

# 또는 프로덕션 빌드 후
cd /opt/url-shortener/vue/dist
pm2 serve . 3000 --name "url-shortener-frontend" --spa

# PM2 관리
pm2 list
pm2 logs url-shortener-frontend
pm2 restart url-shortener-frontend
pm2 stop url-shortener-frontend
```

---

## 🌐 접속 정보

### Local 환경
- 프론트엔드: `http://localhost:3000`
- 백엔드 API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Prod 환경 (NCP VM)
- 프론트엔드: `http://223.130.157.227:3000`
- 백엔드 API: `http://223.130.157.227:8080/api`
- Swagger UI: `http://223.130.157.227:8080/swagger-ui.html`

---

## 🔒 방화벽 설정

NCP 콘솔에서 다음 포트가 열려있는지 확인:
- **3000**: 프론트엔드
- **8080**: 백엔드
- **22**: SSH (관리용)

---

## 🐛 문제 해결

### 백엔드가 시작되지 않는 경우

```bash
# 로그 확인
sudo journalctl -u url-shortener -n 50

# Java 버전 확인
java -version  # Java 17 필요

# 포트 확인
sudo netstat -tlnp | grep 8080
```

### 프론트엔드가 접속되지 않는 경우

```bash
# 포트 확인
sudo netstat -tlnp | grep 3000

# 프로세스 확인
ps aux | grep node

# 환경 변수 확인
echo $VITE_API_BASE_URL
```

### CORS 오류 발생 시

백엔드의 `WebConfig.java`와 모든 컨트롤러의 `@CrossOrigin`에 `http://223.130.157.227:3000`이 포함되어 있는지 확인

---

## 📊 모니터링

### 헬스체크

```bash
# 백엔드
curl http://223.130.157.227:8080/actuator/health

# 프론트엔드
curl http://223.130.157.227:3000
```

### 리소스 모니터링

```bash
# CPU, 메모리
top
htop

# 디스크
df -h

# 네트워크 연결
netstat -an | grep -E "3000|8080"
```

---

## 🔄 업데이트 프로세스

1. 로컬에서 코드 수정 및 테스트
2. Git에 커밋 및 푸시
3. NCP VM에서 Git pull
4. 백엔드 재빌드 및 재시작
5. 프론트엔드 재빌드 및 재시작

```bash
# NCP VM에서
cd /opt/url-shortener
git pull

# 백엔드 재빌드
cd springboot
sudo -u urlshortener mvn clean package -DskipTests
sudo systemctl restart url-shortener

# 프론트엔드 재빌드
cd ../vue
sudo -u urlshortener npm run build
# PM2 사용 시
pm2 restart url-shortener-frontend
```

---

## ✅ 배포 확인 체크리스트

- [ ] 백엔드 서비스 실행 중 (`sudo systemctl status url-shortener`)
- [ ] 프론트엔드 실행 중 (포트 3000 확인)
- [ ] 포트 3000, 8080이 외부에서 접근 가능
- [ ] CORS 설정에 NCP VM IP 포함
- [ ] 환경 변수 설정 완료
- [ ] 헬스체크 통과
- [ ] 로그인 기능 작동 확인

---

**배포 완료 후 접속**: `http://223.130.157.227:3000`

