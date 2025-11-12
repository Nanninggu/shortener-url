#!/bin/bash

# Nginx IPv6 문제 해결 스크립트

set -e

echo "=========================================="
echo "Nginx IPv6 문제 해결"
echo "=========================================="

# 1. Nginx 중지
echo "[1/4] Nginx 중지..."
sudo systemctl stop nginx 2>/dev/null || true

# 2. 기본 설정 파일에서 IPv6 비활성화
echo "[2/4] 기본 설정 파일에서 IPv6 비활성화..."
if [ -f /etc/nginx/sites-enabled/default ]; then
    sudo sed -i 's/listen \[::\]:80 default_server;/# listen [::]:80 default_server;/g' /etc/nginx/sites-enabled/default
    sudo sed -i 's/listen \[::\]:443 ssl default_server;/# listen [::]:443 ssl default_server;/g' /etc/nginx/sites-enabled/default
    echo "기본 설정 파일 수정 완료"
fi

# 3. nginx.conf에서 IPv6 비활성화 (필요한 경우)
echo "[3/4] nginx.conf 확인..."
if [ -f /etc/nginx/nginx.conf ]; then
    # http 블록에서 IPv6 관련 설정 확인
    if grep -q "listen \[::\]:80" /etc/nginx/nginx.conf; then
        echo "nginx.conf에서 IPv6 설정 발견, 주석 처리..."
        sudo sed -i 's/listen \[::\]:80/# listen [::]:80/g' /etc/nginx/nginx.conf
    fi
fi

# 4. Nginx 설정 테스트 및 시작
echo "[4/4] Nginx 설정 테스트..."
if sudo nginx -t; then
    echo "설정이 올바릅니다. Nginx 시작..."
    sudo systemctl start nginx
    sudo systemctl enable nginx
    echo "Nginx 시작 완료!"
else
    echo "설정에 오류가 있습니다. 수동으로 확인해주세요."
    exit 1
fi

echo ""
echo "=========================================="
echo "Nginx IPv6 문제 해결 완료!"
echo "=========================================="
echo ""
echo "Nginx 상태 확인:"
sudo systemctl status nginx --no-pager -l

