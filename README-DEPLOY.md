# NCP 배포 빠른 가이드

## 🚀 한 번에 배포하기

### 1단계: 로컬에서 준비

```bash
# 프로젝트 디렉토리로 이동
cd /Users/may9noy/Documents/shortener-url

# 배포 스크립트 권한 부여
chmod +x deploy.sh quick-deploy.sh
```

### 2단계: 설정 파일 수정 (중요!)

#### `springboot/url-shortener.service` 파일 수정
```ini
Environment="DB_PASSWORD=여기에_안전한_비밀번호_입력"
```

#### `springboot/src/main/resources/application-prod.yml` 파일 수정
```yaml
password: ${DB_PASSWORD:여기에_안전한_비밀번호_입력}
```

### 3단계: 서버로 파일 전송

```bash
# 방법 1: SCP 사용
scp -r springboot vue deploy.sh quick-deploy.sh user@49.50.138.63:/tmp/url-shortener/

# 방법 2: Git 사용 (권장)
# 서버에서:
ssh user@49.50.138.63
git clone <your-repo-url> /tmp/url-shortener
```

### 4단계: 서버에서 배포 실행

```bash
# 서버 접속
ssh user@49.50.138.63

# 배포 스크립트 실행
cd /tmp/url-shortener
chmod +x deploy.sh
sudo ./deploy.sh
```

## ✅ 배포 완료 후 확인

```bash
# 서비스 상태 확인
sudo systemctl status url-shortener
sudo systemctl status nginx

# 애플리케이션 접속
curl http://49.50.138.63/api/actuator/health
```

## 🔄 코드 업데이트 후 재배포

```bash
# 서버 접속
ssh user@49.50.138.63

# 코드 업데이트 (Git 사용 시)
cd /opt/url-shortener
git pull

# 빠른 재배포
sudo ./quick-deploy.sh
```

## 📝 주요 파일 위치

- **애플리케이션**: `/opt/url-shortener`
- **백엔드 JAR**: `/opt/url-shortener/springboot/target/url-shortener-1.0.0.jar`
- **프론트엔드 빌드**: `/opt/url-shortener/vue/dist`
- **로그**: `/opt/url-shortener/logs/application.log`
- **Nginx 설정**: `/etc/nginx/sites-available/url-shortener`
- **서비스 설정**: `/etc/systemd/system/url-shortener.service`

## 🔧 문제 해결

### 서비스가 시작되지 않는 경우
```bash
sudo journalctl -u url-shortener -n 50
```

### 데이터베이스 연결 오류
```bash
sudo systemctl status postgresql
sudo -u postgres psql -d url_shortener
```

### Nginx 오류
```bash
sudo nginx -t
sudo tail -f /var/log/nginx/error.log
```

## 🌐 접속 주소

- **메인**: http://49.50.138.63
- **API**: http://49.50.138.63/api
- **헬스체크**: http://49.50.138.63/api/actuator/health

자세한 내용은 `DEPLOYMENT.md` 파일을 참고하세요.

