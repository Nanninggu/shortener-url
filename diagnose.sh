#!/bin/bash

# 서버 문제 진단 스크립트
# 사용법: 서버에서 sudo bash diagnose.sh 실행

echo "=========================================="
echo "서버 상태 진단 시작"
echo "=========================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 1. 서비스 상태 확인
echo "=== [1/8] 서비스 상태 확인 ==="
echo ""

# 백엔드 서비스
if systemctl is-active --quiet url-shortener; then
    echo -e "${GREEN}✓ 백엔드 서비스: 실행 중${NC}"
else
    echo -e "${RED}✗ 백엔드 서비스: 중지됨${NC}"
fi

# Nginx 서비스
if systemctl is-active --quiet nginx; then
    echo -e "${GREEN}✓ Nginx 서비스: 실행 중${NC}"
else
    echo -e "${RED}✗ Nginx 서비스: 중지됨${NC}"
fi

# PostgreSQL 서비스
if systemctl is-active --quiet postgresql; then
    echo -e "${GREEN}✓ PostgreSQL 서비스: 실행 중${NC}"
else
    echo -e "${RED}✗ PostgreSQL 서비스: 중지됨${NC}"
fi

echo ""

# 2. 포트 확인
echo "=== [2/8] 포트 확인 ==="
echo ""

# 포트 80 (HTTP)
if netstat -tlnp 2>/dev/null | grep -q ":80 " || ss -tlnp 2>/dev/null | grep -q ":80 "; then
    echo -e "${GREEN}✓ 포트 80: 열림${NC}"
    netstat -tlnp 2>/dev/null | grep ":80 " || ss -tlnp 2>/dev/null | grep ":80 "
else
    echo -e "${RED}✗ 포트 80: 닫힘${NC}"
fi

# 포트 8080 (백엔드)
if netstat -tlnp 2>/dev/null | grep -q ":8080 " || ss -tlnp 2>/dev/null | grep -q ":8080 "; then
    echo -e "${GREEN}✓ 포트 8080: 열림${NC}"
    netstat -tlnp 2>/dev/null | grep ":8080 " || ss -tlnp 2>/dev/null | grep ":8080 "
else
    echo -e "${RED}✗ 포트 8080: 닫힘 (백엔드가 실행되지 않음)${NC}"
fi

echo ""

# 3. 프론트엔드 빌드 파일 확인
echo "=== [3/8] 프론트엔드 빌드 파일 확인 ==="
echo ""

FRONTEND_DIR="/opt/url-shortener/vue/dist"
if [ -d "$FRONTEND_DIR" ]; then
    FILE_COUNT=$(find "$FRONTEND_DIR" -type f | wc -l)
    if [ "$FILE_COUNT" -gt 0 ]; then
        echo -e "${GREEN}✓ 프론트엔드 빌드 파일 존재 ($FILE_COUNT 개 파일)${NC}"
        if [ -f "$FRONTEND_DIR/index.html" ]; then
            echo -e "${GREEN}  ✓ index.html 존재${NC}"
        else
            echo -e "${RED}  ✗ index.html 없음${NC}"
        fi
    else
        echo -e "${RED}✗ 프론트엔드 빌드 디렉토리가 비어있음${NC}"
    fi
else
    echo -e "${RED}✗ 프론트엔드 빌드 디렉토리 없음: $FRONTEND_DIR${NC}"
fi

echo ""

# 4. 백엔드 JAR 파일 확인
echo "=== [4/8] 백엔드 JAR 파일 확인 ==="
echo ""

JAR_FILE="/opt/url-shortener/springboot/target/url-shortener-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    echo -e "${GREEN}✓ JAR 파일 존재${NC}"
    ls -lh "$JAR_FILE"
else
    echo -e "${RED}✗ JAR 파일 없음: $JAR_FILE${NC}"
fi

echo ""

# 5. 로컬 연결 테스트
echo "=== [5/8] 로컬 연결 테스트 ==="
echo ""

# 백엔드 헬스체크
echo "백엔드 헬스체크..."
if curl -s -m 5 http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 백엔드 응답 성공${NC}"
    curl -s http://localhost:8080/actuator/health | head -5
else
    echo -e "${RED}✗ 백엔드 응답 실패${NC}"
fi

echo ""

# Nginx 응답
echo "Nginx 응답 테스트..."
if curl -s -m 5 http://localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Nginx 응답 성공${NC}"
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost)
    echo "  HTTP 상태 코드: $HTTP_CODE"
else
    echo -e "${RED}✗ Nginx 응답 실패${NC}"
fi

echo ""

# 6. Nginx 설정 확인
echo "=== [6/8] Nginx 설정 확인 ==="
echo ""

if [ -f "/etc/nginx/sites-available/url-shortener" ]; then
    echo -e "${GREEN}✓ Nginx 설정 파일 존재${NC}"
    
    # 설정 테스트
    if sudo nginx -t 2>&1 | grep -q "successful"; then
        echo -e "${GREEN}✓ Nginx 설정 문법 올바름${NC}"
    else
        echo -e "${RED}✗ Nginx 설정 문법 오류${NC}"
        sudo nginx -t
    fi
else
    echo -e "${RED}✗ Nginx 설정 파일 없음${NC}"
fi

echo ""

# 7. 최근 로그 확인
echo "=== [7/8] 최근 로그 확인 ==="
echo ""

# 백엔드 로그
echo "백엔드 로그 (최근 10줄):"
if [ -f "/opt/url-shortener/logs/application.log" ]; then
    tail -10 /opt/url-shortener/logs/application.log 2>/dev/null || echo "로그 파일 없음"
else
    echo "로그 파일 없음, systemd 로그 확인:"
    sudo journalctl -u url-shortener -n 10 --no-pager 2>/dev/null | tail -10 || echo "로그 없음"
fi

echo ""

# Nginx 에러 로그
echo "Nginx 에러 로그 (최근 10줄):"
if [ -f "/var/log/nginx/url-shortener-error.log" ]; then
    tail -10 /var/log/nginx/url-shortener-error.log 2>/dev/null || echo "에러 로그 없음"
elif [ -f "/var/log/nginx/error.log" ]; then
    tail -10 /var/log/nginx/error.log 2>/dev/null || echo "에러 로그 없음"
else
    echo "에러 로그 파일 없음"
fi

echo ""

# 8. 방화벽 확인
echo "=== [8/8] 방화벽 확인 ==="
echo ""

if command -v ufw &> /dev/null; then
    UFW_STATUS=$(sudo ufw status | head -1)
    echo "UFW 상태: $UFW_STATUS"
    
    if echo "$UFW_STATUS" | grep -q "active"; then
        echo "방화벽 규칙:"
        sudo ufw status numbered | grep -E "80|443|8080" || echo "포트 80, 443, 8080 규칙 없음"
    fi
else
    echo "UFW가 설치되지 않음"
fi

echo ""
echo "=========================================="
echo "진단 완료"
echo "=========================================="
echo ""
echo "문제 해결 방법:"
echo ""
echo "1. 백엔드가 실행되지 않는 경우:"
echo "   sudo systemctl start url-shortener"
echo "   sudo journalctl -u url-shortener -f"
echo ""
echo "2. Nginx가 실행되지 않는 경우:"
echo "   sudo systemctl start nginx"
echo "   sudo nginx -t"
echo ""
echo "3. 프론트엔드 빌드가 없는 경우:"
echo "   cd /opt/url-shortener/vue"
echo "   npm run build"
echo ""
echo "4. 포트가 열리지 않는 경우:"
echo "   sudo ufw allow 80/tcp"
echo "   sudo ufw allow 443/tcp"
echo ""

