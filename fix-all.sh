#!/bin/bash

# 모든 문제 자동 해결 스크립트
# 사용법: 서버에서 sudo bash fix-all.sh 실행

set -e

echo "=========================================="
echo "자동 문제 해결 시작"
echo "=========================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

APP_DIR="/opt/url-shortener"
BACKEND_DIR="$APP_DIR/springboot"
FRONTEND_DIR="$APP_DIR/vue"

# 1. 서비스 시작
echo -e "${YELLOW}[1/6] 서비스 시작...${NC}"

# H2 데이터베이스 디렉토리 확인
H2_DATA_DIR="$APP_DIR/data"
if [ ! -d "$H2_DATA_DIR" ]; then
    echo "H2 데이터베이스 디렉토리 생성 중..."
    sudo mkdir -p $H2_DATA_DIR
    sudo chown -R urlshortener:urlshortener $H2_DATA_DIR 2>/dev/null || true
fi

# 백엔드 시작
if ! systemctl is-active --quiet url-shortener; then
    echo "백엔드 서비스 시작 중..."
    sudo systemctl start url-shortener
    sleep 5
fi

# Nginx 시작
if ! systemctl is-active --quiet nginx; then
    echo "Nginx 시작 중..."
    sudo systemctl start nginx
    sleep 2
fi

echo ""

# 2. 프론트엔드 빌드 확인 및 재빌드
echo -e "${YELLOW}[2/6] 프론트엔드 빌드 확인...${NC}"

if [ ! -d "$FRONTEND_DIR/dist" ] || [ ! -f "$FRONTEND_DIR/dist/index.html" ]; then
    echo "프론트엔드 빌드가 없습니다. 빌드 시작..."
    cd $FRONTEND_DIR
    
    # vite.config.js에서 terser 확인
    if grep -q "minify: 'terser'" vite.config.js 2>/dev/null; then
        echo "vite.config.js 수정 중..."
        sed -i "s/minify: 'terser'/minify: 'esbuild'/g" vite.config.js
    fi
    
    npm install
    npm run build
    
    if [ -d "$FRONTEND_DIR/dist" ]; then
        echo -e "${GREEN}✓ 프론트엔드 빌드 완료${NC}"
    else
        echo -e "${RED}✗ 프론트엔드 빌드 실패${NC}"
    fi
    cd -
else
    echo -e "${GREEN}✓ 프론트엔드 빌드 파일 존재${NC}"
fi

echo ""

# 3. Nginx 설정 확인 및 수정
echo -e "${YELLOW}[3/6] Nginx 설정 확인...${NC}"

# Nginx 설정 테스트
if ! sudo nginx -t 2>/dev/null; then
    echo "Nginx 설정 오류 발견. 수정 중..."
    
    # IPv6 비활성화
    if [ -f /etc/nginx/sites-enabled/default ]; then
        sudo sed -i 's/listen \[::\]:80/# listen [::]:80/g' /etc/nginx/sites-enabled/default
    fi
    
    # 설정 파일 확인
    if [ -f /etc/nginx/sites-available/url-shortener ]; then
        # root 경로 확인
        if ! grep -q "root /opt/url-shortener/vue/dist" /etc/nginx/sites-available/url-shortener; then
            echo "Nginx 설정 파일 수정 중..."
            sudo cp $BACKEND_DIR/nginx.conf /etc/nginx/sites-available/url-shortener
        fi
    fi
    
    sudo nginx -t
fi

# Nginx 재시작
sudo systemctl restart nginx
sleep 2

echo ""

# 4. 백엔드 헬스체크
echo -e "${YELLOW}[4/6] 백엔드 헬스체크...${NC}"

sleep 3
if curl -s -m 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 백엔드 정상 작동${NC}"
else
    echo -e "${RED}✗ 백엔드 응답 없음${NC}"
    echo "백엔드 로그 확인:"
    sudo journalctl -u url-shortener -n 20 --no-pager | tail -10
fi

echo ""

# 5. 방화벽 규칙 설정 (활성화하지 않음)
echo -e "${YELLOW}[5/6] 방화벽 규칙 설정...${NC}"

if command -v ufw &> /dev/null; then
    sudo ufw --force allow 80/tcp
    sudo ufw --force allow 443/tcp
    sudo ufw --force allow 8080/tcp
    echo -e "${GREEN}✓ 방화벽 규칙 설정 완료 (방화벽은 활성화하지 않음)${NC}"
    echo "필요한 경우 수동으로 'sudo ufw enable'을 실행하세요."
else
    echo -e "${YELLOW}⚠ UFW가 설치되지 않음. NCP 콘솔에서 포트를 열어주세요.${NC}"
fi

echo ""

# 6. 최종 확인
echo -e "${YELLOW}[6/6] 최종 확인...${NC}"
echo ""

# 서비스 상태
echo "서비스 상태:"
if systemctl is-active --quiet url-shortener; then
    echo -e "${GREEN}✓ 백엔드: 실행 중${NC}"
else
    echo -e "${RED}✗ 백엔드: 중지됨${NC}"
fi

if systemctl is-active --quiet nginx; then
    echo -e "${GREEN}✓ Nginx: 실행 중${NC}"
else
    echo -e "${RED}✗ Nginx: 중지됨${NC}"
fi

echo ""

# 연결 테스트
echo "연결 테스트:"
if curl -s -m 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 백엔드 로컬 연결 성공${NC}"
else
    echo -e "${RED}✗ 백엔드 로컬 연결 실패${NC}"
fi

if curl -s -m 5 http://localhost > /dev/null 2>&1; then
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost)
    echo -e "${GREEN}✓ Nginx 로컬 연결 성공 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "${RED}✗ Nginx 로컬 연결 실패${NC}"
fi

echo ""
echo "=========================================="
echo "자동 해결 완료"
echo "=========================================="
echo ""
echo "외부 접속 테스트:"
echo "  curl http://49.50.138.63"
echo "  curl http://49.50.138.63/api/actuator/health"
echo ""
echo "문제가 계속되면 다음을 확인하세요:"
echo "  1. NCP 콘솔에서 포트 80, 443이 열려있는지 확인"
echo "  2. sudo journalctl -u url-shortener -f (백엔드 로그)"
echo "  3. sudo tail -f /var/log/nginx/error.log (Nginx 에러 로그)"
echo ""

