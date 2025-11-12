#!/bin/bash

# NCP 배포 스크립트
# 사용법: ./deploy.sh

set -e  # 에러 발생 시 스크립트 중단

echo "=========================================="
echo "H-Link URL Shortener 배포 시작"
echo "=========================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 변수 설정
APP_DIR="/opt/url-shortener"
BACKEND_DIR="$APP_DIR/springboot"
FRONTEND_DIR="$APP_DIR/vue"
SERVICE_USER="urlshortener"
PUBLIC_IP="49.50.138.63"

# 1. 필수 패키지 설치 확인
echo -e "${YELLOW}[1/8] 필수 패키지 확인 및 설치...${NC}"

# dpkg 잠금 해제 (필요한 경우)
sudo dpkg --configure -a

# nginx가 실행 중이면 중지
if systemctl is-active --quiet nginx; then
    echo "Nginx가 실행 중입니다. 중지합니다..."
    sudo systemctl stop nginx
fi

# nginx 설정 문제 해결
if [ -f /etc/nginx/sites-enabled/default ]; then
    echo "기본 nginx 설정 파일 제거..."
    sudo rm -f /etc/nginx/sites-enabled/default
fi

# 패키지 설치
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk maven nodejs npm postgresql postgresql-contrib git

# nginx 설치 (별도로 처리)
echo "Nginx 설치 중..."
sudo apt-get install -y nginx || {
    echo -e "${RED}Nginx 설치 실패. 수동으로 수정합니다...${NC}"
    sudo dpkg --configure -a
    sudo apt-get install -f -y
    sudo apt-get install -y nginx
}

# 2. 사용자 생성 (없는 경우)
echo -e "${YELLOW}[2/8] 서비스 사용자 생성...${NC}"
if ! id "$SERVICE_USER" &>/dev/null; then
    sudo useradd -r -s /bin/bash -d $APP_DIR $SERVICE_USER
    echo "사용자 $SERVICE_USER 생성 완료"
else
    echo "사용자 $SERVICE_USER 이미 존재"
fi

# 3. 애플리케이션 디렉토리 생성
echo -e "${YELLOW}[3/8] 애플리케이션 디렉토리 생성...${NC}"
sudo mkdir -p $APP_DIR
sudo mkdir -p $APP_DIR/logs
sudo chown -R $SERVICE_USER:$SERVICE_USER $APP_DIR

# 4. 프로젝트 파일 복사 (현재 디렉토리에서)
echo -e "${YELLOW}[4/8] 프로젝트 파일 복사...${NC}"
sudo -u $SERVICE_USER cp -r springboot $APP_DIR/
sudo -u $SERVICE_USER cp -r vue $APP_DIR/

# 5. PostgreSQL 데이터베이스 설정
echo -e "${YELLOW}[5/8] PostgreSQL 데이터베이스 설정...${NC}"
sudo -u postgres psql <<EOF
-- 데이터베이스 생성 (이미 있으면 무시)
SELECT 'CREATE DATABASE url_shortener'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'url_shortener')\gexec

-- 사용자 생성 (필요한 경우)
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'urlshortener') THEN
    CREATE USER urlshortener WITH PASSWORD 'your_secure_password_here';
  END IF;
END
\$\$;

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE url_shortener TO urlshortener;
\q
EOF

# 스키마 실행
sudo -u postgres psql -d url_shortener -f $BACKEND_DIR/src/main/resources/db/schema.sql

# 6. 백엔드 빌드
echo -e "${YELLOW}[6/8] 백엔드 빌드...${NC}"
cd $BACKEND_DIR
sudo -u $SERVICE_USER mvn clean package -DskipTests
cd -

# 7. 프론트엔드 빌드
echo -e "${YELLOW}[7/8] 프론트엔드 빌드...${NC}"
cd $FRONTEND_DIR
sudo -u $SERVICE_USER npm install
sudo -u $SERVICE_USER npm run build
cd -

# 8. systemd 서비스 설정
echo -e "${YELLOW}[8/8] systemd 서비스 설정...${NC}"
sudo cp $APP_DIR/springboot/url-shortener.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable url-shortener
sudo systemctl restart url-shortener

# 9. Nginx 설정
echo -e "${YELLOW}[9/9] Nginx 설정...${NC}"

# 기존 설정 파일 백업
if [ -f /etc/nginx/sites-available/url-shortener ]; then
    sudo cp /etc/nginx/sites-available/url-shortener /etc/nginx/sites-available/url-shortener.backup
fi

# Nginx 설정 파일 복사
sudo cp $BACKEND_DIR/nginx.conf /etc/nginx/sites-available/url-shortener

# 기본 설정 제거
sudo rm -f /etc/nginx/sites-enabled/default

# 심볼릭 링크 생성
sudo ln -sf /etc/nginx/sites-available/url-shortener /etc/nginx/sites-enabled/url-shortener

# Nginx 설정 테스트
echo "Nginx 설정 테스트 중..."
if sudo nginx -t; then
    echo -e "${GREEN}Nginx 설정이 올바릅니다.${NC}"
    sudo systemctl restart nginx
    sudo systemctl enable nginx
else
    echo -e "${RED}Nginx 설정에 오류가 있습니다. 수동으로 확인해주세요.${NC}"
    echo "설정 파일: /etc/nginx/sites-available/url-shortener"
    exit 1
fi

# 10. 방화벽 설정
echo -e "${YELLOW}[10/10] 방화벽 설정...${NC}"
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw --force enable

echo ""
echo -e "${GREEN}=========================================="
echo "배포 완료!"
echo "==========================================${NC}"
echo ""
echo "서비스 상태 확인:"
echo "  sudo systemctl status url-shortener"
echo "  sudo systemctl status nginx"
echo ""
echo "로그 확인:"
echo "  sudo journalctl -u url-shortener -f"
echo ""
echo "애플리케이션 접속:"
echo "  http://$PUBLIC_IP"
echo ""

