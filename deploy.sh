#!/bin/bash

# NCP 배포 스크립트
# 사용법: ./deploy.sh

# 에러 발생 시에도 계속 진행하되, 중요한 단계는 체크
set +e  # 에러 발생 시에도 계속 진행

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

# Nginx IPv6 문제 해결
echo "Nginx IPv6 설정 수정 중..."
# 기본 설정 파일에서 IPv6 비활성화
if [ -f /etc/nginx/sites-enabled/default ]; then
    sudo sed -i 's/listen \[::\]:80 default_server;/# listen [::]:80 default_server;/g' /etc/nginx/sites-enabled/default
    sudo sed -i 's/listen \[::\]:443 ssl default_server;/# listen [::]:443 ssl default_server;/g' /etc/nginx/sites-enabled/default
    echo "기본 설정 파일에서 IPv6 비활성화 완료"
fi

# sites-available의 default 파일도 수정
if [ -f /etc/nginx/sites-available/default ]; then
    sudo sed -i 's/listen \[::\]:80 default_server;/# listen [::]:80 default_server;/g' /etc/nginx/sites-available/default
    sudo sed -i 's/listen \[::\]:443 ssl default_server;/# listen [::]:443 ssl default_server;/g' /etc/nginx/sites-available/default
    echo "sites-available 설정 파일에서 IPv6 비활성화 완료"
fi

# Nginx 설정 테스트
if sudo nginx -t 2>/dev/null; then
    echo "Nginx 설정이 올바릅니다."
    sudo systemctl start nginx || true
else
    echo "Nginx 설정 테스트 실패, 계속 진행..."
fi

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
echo -e "${YELLOW}[5/10] PostgreSQL 데이터베이스 설정...${NC}"

# PostgreSQL 서비스 시작 확인
if ! systemctl is-active --quiet postgresql; then
    echo "PostgreSQL 서비스 시작 중..."
    sudo systemctl start postgresql
    sleep 2
fi

# 데이터베이스 및 사용자 생성
sudo -u postgres psql <<EOF 2>/dev/null
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
ALTER DATABASE url_shortener OWNER TO urlshortener;
\q
EOF

# 스키마 실행 (테이블이 없을 때만)
if [ -f "$BACKEND_DIR/src/main/resources/db/schema.sql" ]; then
    echo "데이터베이스 스키마 확인 중..."
    TABLE_COUNT=$(sudo -u postgres psql -d url_shortener -tAc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null || echo "0")
    if [ "$TABLE_COUNT" = "0" ] || [ -z "$TABLE_COUNT" ]; then
        echo "스키마 실행 중..."
        sudo -u postgres psql -d url_shortener -f $BACKEND_DIR/src/main/resources/db/schema.sql 2>/dev/null || {
            echo -e "${YELLOW}스키마 실행 중 일부 오류가 발생했을 수 있습니다. 계속 진행합니다.${NC}"
        }
    else
        echo "데이터베이스 테이블이 이미 존재합니다. 스키마 실행을 건너뜁니다."
    fi
fi

# 6. 백엔드 빌드
echo -e "${YELLOW}[6/10] 백엔드 빌드...${NC}"
cd $BACKEND_DIR

# Maven이 설치되어 있는지 확인
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven이 설치되지 않았습니다. 설치 중...${NC}"
    sudo apt-get install -y maven
fi

# Java 버전 확인
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}Java 17 이상이 필요합니다. 현재 버전: $JAVA_VERSION${NC}"
    exit 1
fi

echo "백엔드 빌드 중... (시간이 걸릴 수 있습니다)"
if sudo -u $SERVICE_USER mvn clean package -DskipTests; then
    echo -e "${GREEN}백엔드 빌드 완료${NC}"
else
    echo -e "${RED}백엔드 빌드 실패!${NC}"
    exit 1
fi

cd -

# 7. 프론트엔드 빌드
echo -e "${YELLOW}[7/10] 프론트엔드 빌드...${NC}"
cd $FRONTEND_DIR

# Node.js가 설치되어 있는지 확인
if ! command -v node &> /dev/null; then
    echo -e "${RED}Node.js가 설치되지 않았습니다. 설치 중...${NC}"
    curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
    sudo apt-get install -y nodejs
fi

# Node.js 버전 확인
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo -e "${YELLOW}Node.js 버전이 낮습니다. 업그레이드를 권장합니다.${NC}"
fi

# npm 캐시 정리 및 의존성 설치
echo "npm 의존성 설치 중..."
sudo -u $SERVICE_USER npm cache clean --force 2>/dev/null || true

if sudo -u $SERVICE_USER npm install; then
    echo "의존성 설치 완료"
else
    echo -e "${YELLOW}의존성 설치 중 일부 경고가 발생했을 수 있습니다. 계속 진행합니다.${NC}"
fi

# vite.config.js에서 terser를 esbuild로 변경 확인
if grep -q "minify: 'terser'" vite.config.js 2>/dev/null; then
    echo "vite.config.js에서 terser를 esbuild로 변경 중..."
    sudo -u $SERVICE_USER sed -i "s/minify: 'terser'/minify: 'esbuild'/g" vite.config.js
fi

# 빌드 실행
echo "프론트엔드 빌드 중... (시간이 걸릴 수 있습니다)"
if sudo -u $SERVICE_USER npm run build; then
    if [ -d "$FRONTEND_DIR/dist" ]; then
        echo -e "${GREEN}프론트엔드 빌드 완료${NC}"
    else
        echo -e "${RED}프론트엔드 빌드 실패: dist 디렉토리가 생성되지 않았습니다!${NC}"
        exit 1
    fi
else
    echo -e "${RED}프론트엔드 빌드 실패!${NC}"
    exit 1
fi

cd -

# 8. systemd 서비스 설정
echo -e "${YELLOW}[8/10] systemd 서비스 설정...${NC}"

# JAR 파일 확인
JAR_FILE="$BACKEND_DIR/target/url-shortener-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}JAR 파일을 찾을 수 없습니다: $JAR_FILE${NC}"
    exit 1
fi

# 서비스 파일 복사
if [ -f "$BACKEND_DIR/url-shortener.service" ]; then
    sudo cp $BACKEND_DIR/url-shortener.service /etc/systemd/system/
    sudo systemctl daemon-reload
    sudo systemctl enable url-shortener
    
    # 기존 서비스가 실행 중이면 중지
    if systemctl is-active --quiet url-shortener; then
        echo "기존 서비스 중지 중..."
        sudo systemctl stop url-shortener
        sleep 2
    fi
    
    # 서비스 시작
    echo "백엔드 서비스 시작 중..."
    if sudo systemctl start url-shortener; then
        sleep 3
        if systemctl is-active --quiet url-shortener; then
            echo -e "${GREEN}백엔드 서비스 시작 완료${NC}"
        else
            echo -e "${YELLOW}서비스 시작 확인 중...${NC}"
            sleep 2
            if systemctl is-active --quiet url-shortener; then
                echo -e "${GREEN}백엔드 서비스 시작 완료${NC}"
            else
                echo -e "${RED}서비스 시작 실패. 로그를 확인하세요: sudo journalctl -u url-shortener -n 50${NC}"
            fi
        fi
    else
        echo -e "${RED}서비스 시작 실패!${NC}"
        exit 1
    fi
else
    echo -e "${RED}서비스 파일을 찾을 수 없습니다: $BACKEND_DIR/url-shortener.service${NC}"
    exit 1
fi

# 9. Nginx 설정
echo -e "${YELLOW}[9/10] Nginx 설정...${NC}"

# 프론트엔드 빌드 확인
if [ ! -d "$FRONTEND_DIR/dist" ]; then
    echo -e "${RED}프론트엔드 빌드 디렉토리를 찾을 수 없습니다: $FRONTEND_DIR/dist${NC}"
    exit 1
fi

# 기존 설정 파일 백업
if [ -f /etc/nginx/sites-available/url-shortener ]; then
    sudo cp /etc/nginx/sites-available/url-shortener /etc/nginx/sites-available/url-shortener.backup.$(date +%Y%m%d_%H%M%S)
fi

# Nginx 설정 파일 복사
if [ -f "$BACKEND_DIR/nginx.conf" ]; then
    sudo cp $BACKEND_DIR/nginx.conf /etc/nginx/sites-available/url-shortener
    
    # 기본 설정 제거
    sudo rm -f /etc/nginx/sites-enabled/default
    
    # 심볼릭 링크 생성
    sudo ln -sf /etc/nginx/sites-available/url-shortener /etc/nginx/sites-enabled/url-shortener
    
    # Nginx 설정 테스트
    echo "Nginx 설정 테스트 중..."
    if sudo nginx -t 2>&1; then
        echo -e "${GREEN}Nginx 설정이 올바릅니다.${NC}"
        
        # Nginx 재시작
        if sudo systemctl restart nginx; then
            sleep 2
            if systemctl is-active --quiet nginx; then
                echo -e "${GREEN}Nginx 시작 완료${NC}"
                sudo systemctl enable nginx
            else
                echo -e "${YELLOW}Nginx 시작 확인 중...${NC}"
                sleep 2
                if systemctl is-active --quiet nginx; then
                    echo -e "${GREEN}Nginx 시작 완료${NC}"
                else
                    echo -e "${RED}Nginx 시작 실패. 로그를 확인하세요: sudo journalctl -u nginx -n 50${NC}"
                    exit 1
                fi
            fi
        else
            echo -e "${RED}Nginx 재시작 실패!${NC}"
            exit 1
        fi
    else
        echo -e "${RED}Nginx 설정에 오류가 있습니다.${NC}"
        echo "설정 파일: /etc/nginx/sites-available/url-shortener"
        sudo nginx -t
        exit 1
    fi
else
    echo -e "${RED}Nginx 설정 파일을 찾을 수 없습니다: $BACKEND_DIR/nginx.conf${NC}"
    exit 1
fi

# 10. 방화벽 설정
echo -e "${YELLOW}[10/10] 방화벽 설정...${NC}"

# UFW가 설치되어 있는지 확인
if command -v ufw &> /dev/null; then
    echo "방화벽 규칙 설정 중..."
    sudo ufw --force allow 22/tcp   # SSH
    sudo ufw --force allow 80/tcp   # HTTP
    sudo ufw --force allow 443/tcp  # HTTPS
    
    # UFW 활성화 (비활성화되어 있는 경우)
    if ! sudo ufw status | grep -q "Status: active"; then
        echo "방화벽 활성화 중..."
        echo "y" | sudo ufw enable
    else
        echo "방화벽이 이미 활성화되어 있습니다."
    fi
else
    echo -e "${YELLOW}UFW가 설치되지 않았습니다. 방화벽 설정을 건너뜁니다.${NC}"
    echo "NCP 콘솔에서 포트 80, 443을 열어주세요."
fi

# 최종 상태 확인
echo ""
echo -e "${GREEN}=========================================="
echo "배포 완료!"
echo "==========================================${NC}"
echo ""

# 서비스 상태 확인
echo "서비스 상태 확인 중..."
sleep 2

BACKEND_STATUS="실패"
NGINX_STATUS="실패"

if systemctl is-active --quiet url-shortener; then
    BACKEND_STATUS="실행 중"
    echo -e "${GREEN}✓ 백엔드 서비스: $BACKEND_STATUS${NC}"
else
    echo -e "${RED}✗ 백엔드 서비스: $BACKEND_STATUS${NC}"
fi

if systemctl is-active --quiet nginx; then
    NGINX_STATUS="실행 중"
    echo -e "${GREEN}✓ Nginx: $NGINX_STATUS${NC}"
else
    echo -e "${RED}✗ Nginx: $NGINX_STATUS${NC}"
fi

echo ""
echo "=========================================="
echo "유용한 명령어"
echo "=========================================="
echo "서비스 상태 확인:"
echo "  sudo systemctl status url-shortener"
echo "  sudo systemctl status nginx"
echo ""
echo "로그 확인:"
echo "  sudo journalctl -u url-shortener -f"
echo "  sudo tail -f /var/log/nginx/url-shortener-error.log"
echo ""
echo "서비스 재시작:"
echo "  sudo systemctl restart url-shortener"
echo "  sudo systemctl restart nginx"
echo ""
echo "애플리케이션 접속:"
echo "  http://$PUBLIC_IP"
echo "  http://$PUBLIC_IP/api/actuator/health"
echo ""

# 헬스체크 시도
echo "헬스체크 시도 중..."
sleep 3
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 백엔드 헬스체크 성공${NC}"
else
    echo -e "${YELLOW}⚠ 백엔드 헬스체크 실패 (서비스가 아직 시작 중일 수 있습니다)${NC}"
fi

if curl -s http://localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Nginx 응답 확인${NC}"
else
    echo -e "${YELLOW}⚠ Nginx 응답 확인 실패${NC}"
fi

echo ""
echo -e "${GREEN}배포 스크립트 실행 완료!${NC}"
echo ""

