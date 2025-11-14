#!/bin/bash

# 서비스 중지 스크립트
# 사용법: sudo ./stop.sh

echo "=========================================="
echo "H-Link URL Shortener 서비스 중지"
echo "=========================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 백엔드 서비스 중지
echo -e "${YELLOW}[1/2] 백엔드 서비스 중지 중...${NC}"
if systemctl is-active --quiet url-shortener; then
    sudo systemctl stop url-shortener
    sleep 2
    if systemctl is-active --quiet url-shortener; then
        echo -e "${RED}✗ 백엔드 서비스 중지 실패${NC}"
    else
        echo -e "${GREEN}✓ 백엔드 서비스 중지 완료${NC}"
    fi
else
    echo -e "${YELLOW}⚠ 백엔드 서비스가 이미 중지되어 있습니다${NC}"
fi

echo ""

# Nginx 중지
echo -e "${YELLOW}[2/2] Nginx 중지 중...${NC}"
if systemctl is-active --quiet nginx; then
    sudo systemctl stop nginx
    sleep 1
    if systemctl is-active --quiet nginx; then
        echo -e "${RED}✗ Nginx 중지 실패${NC}"
    else
        echo -e "${GREEN}✓ Nginx 중지 완료${NC}"
    fi
else
    echo -e "${YELLOW}⚠ Nginx가 이미 중지되어 있습니다${NC}"
fi

echo ""
echo "=========================================="
echo "서비스 중지 완료"
echo "=========================================="
echo ""

# 최종 상태 확인
echo "최종 서비스 상태:"
if systemctl is-active --quiet url-shortener; then
    echo -e "${RED}✗ 백엔드: 아직 실행 중${NC}"
else
    echo -e "${GREEN}✓ 백엔드: 중지됨${NC}"
fi

if systemctl is-active --quiet nginx; then
    echo -e "${RED}✗ Nginx: 아직 실행 중${NC}"
else
    echo -e "${GREEN}✓ Nginx: 중지됨${NC}"
fi

echo ""
echo "서비스를 다시 시작하려면:"
echo "  sudo systemctl start url-shortener"
echo "  sudo systemctl start nginx"
echo ""
echo "또는 배포 스크립트 실행:"
echo "  sudo ./deploy.sh"
echo ""

