# NCP 배포 가이드

## 서버 정보
- **OS**: Ubuntu 24.04
- **퍼블릭 IP**: 49.50.138.63
- **애플리케이션 디렉토리**: `/opt/url-shortener`

## 배포 전 준비사항

### 1. 로컬에서 프로젝트 준비

```bash
# 프로젝트 디렉토리로 이동
cd /Users/may9noy/Documents/shortener-url

# 배포 스크립트에 실행 권한 부여
chmod +x deploy.sh
chmod +x quick-deploy.sh
chmod +x springboot/url-shortener.service
```

### 2. 설정 파일 수정

#### `springboot/url-shortener.service` 수정
- `DB_PASSWORD`: PostgreSQL 비밀번호 설정
- `JWT_SECRET`: JWT 시크릿 키 변경 (보안)

#### `springboot/src/main/resources/application-prod.yml` 수정
- `DB_PASSWORD`: PostgreSQL 비밀번호 설정
- `JWT_SECRET`: JWT 시크릿 키 변경 (보안)

## 배포 방법

### 방법 1: 전체 배포 (첫 배포)

1. **프로젝트 파일을 서버로 전송**

```bash
# SCP로 파일 전송
scp -r springboot vue deploy.sh quick-deploy.sh user@49.50.138.63:/tmp/url-shortener/

# 또는 Git 사용 (권장)
# 서버에서:
git clone <your-repo-url> /tmp/url-shortener
```

2. **서버에 접속**

```bash
ssh user@49.50.138.63
```

3. **배포 스크립트 실행**

```bash
cd /tmp/url-shortener
chmod +x deploy.sh
sudo ./deploy.sh
```

### 방법 2: 빠른 재배포 (코드 변경 후)

```bash
# 서버에 접속
ssh user@49.50.138.63

# 프로젝트 업데이트 (Git 사용 시)
cd /opt/url-shortener
git pull

# 빠른 배포 스크립트 실행
sudo ./quick-deploy.sh
```

## 수동 배포 단계

### 1. 필수 패키지 설치

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk maven nodejs npm nginx postgresql postgresql-contrib
```

### 2. PostgreSQL 설정

```bash
# PostgreSQL 접속
sudo -u postgres psql

# 데이터베이스 생성
CREATE DATABASE url_shortener;

# 사용자 생성
CREATE USER urlshortener WITH PASSWORD 'your_secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE url_shortener TO urlshortener;

# 스키마 실행
\c url_shortener
\i /opt/url-shortener/springboot/src/main/resources/db/schema.sql
\q
```

### 3. 백엔드 빌드 및 실행

```bash
cd /opt/url-shortener/springboot
mvn clean package -DskipTests

# systemd 서비스 시작
sudo systemctl start url-shortener
sudo systemctl enable url-shortener
```

### 4. 프론트엔드 빌드

```bash
cd /opt/url-shortener/vue
npm install
npm run build
```

### 5. Nginx 설정

```bash
# Nginx 설정 파일 복사
sudo cp /opt/url-shortener/springboot/nginx.conf /etc/nginx/sites-available/url-shortener
sudo ln -s /etc/nginx/sites-available/url-shortener /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# Nginx 테스트 및 재시작
sudo nginx -t
sudo systemctl restart nginx
```

### 6. 방화벽 설정

```bash
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

## 서비스 관리

### 백엔드 서비스

```bash
# 서비스 상태 확인
sudo systemctl status url-shortener

# 서비스 시작
sudo systemctl start url-shortener

# 서비스 중지
sudo systemctl stop url-shortener

# 서비스 재시작
sudo systemctl restart url-shortener

# 로그 확인
sudo journalctl -u url-shortener -f
```

### Nginx

```bash
# 상태 확인
sudo systemctl status nginx

# 재시작
sudo systemctl restart nginx

# 설정 테스트
sudo nginx -t

# 로그 확인
sudo tail -f /var/log/nginx/url-shortener-access.log
sudo tail -f /var/log/nginx/url-shortener-error.log
```

## 환경 변수 설정

`/etc/systemd/system/url-shortener.service` 파일에서 환경 변수 설정:

```ini
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_USERNAME=urlshortener"
Environment="DB_PASSWORD=your_secure_password_here"
Environment="APP_BASE_URL=http://49.50.138.63"
Environment="JWT_SECRET=your_jwt_secret_key_here"
```

변경 후:
```bash
sudo systemctl daemon-reload
sudo systemctl restart url-shortener
```

## SSL 인증서 설정 (선택사항)

Let's Encrypt 사용:

```bash
# Certbot 설치
sudo apt-get install -y certbot python3-certbot-nginx

# 인증서 발급
sudo certbot --nginx -d 49.50.138.63

# 자동 갱신 테스트
sudo certbot renew --dry-run
```

인증서 발급 후 `nginx.conf`의 HTTPS 설정 주석 해제.

## 문제 해결

### 백엔드가 시작되지 않는 경우

```bash
# 로그 확인
sudo journalctl -u url-shortener -n 50

# Java 버전 확인
java -version  # Java 17 필요

# 포트 확인
sudo netstat -tlnp | grep 8080
```

### 데이터베이스 연결 오류

```bash
# PostgreSQL 상태 확인
sudo systemctl status postgresql

# 연결 테스트
psql -U urlshortener -d url_shortener -h localhost
```

### Nginx 오류

```bash
# 설정 파일 문법 확인
sudo nginx -t

# 에러 로그 확인
sudo tail -f /var/log/nginx/error.log
```

## 모니터링

### 헬스체크

```bash
# 백엔드 헬스체크
curl http://localhost:8080/actuator/health

# 전체 애플리케이션
curl http://49.50.138.63/api/actuator/health
```

### 리소스 모니터링

```bash
# CPU, 메모리 사용량
top
htop

# 디스크 사용량
df -h

# 네트워크 연결
netstat -an | grep :8080
```

## 백업

### 데이터베이스 백업

```bash
# 백업
sudo -u postgres pg_dump url_shortener > backup_$(date +%Y%m%d).sql

# 복원
sudo -u postgres psql url_shortener < backup_20240101.sql
```

## 업데이트 프로세스

1. 코드 변경
2. Git pull 또는 파일 업로드
3. `quick-deploy.sh` 실행
4. 서비스 상태 확인

## 접속 정보

- **애플리케이션**: http://49.50.138.63
- **API**: http://49.50.138.63/api
- **헬스체크**: http://49.50.138.63/api/actuator/health

