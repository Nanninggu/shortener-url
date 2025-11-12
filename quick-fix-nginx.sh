#!/bin/bash

# Nginx IPv6 문제 빠른 해결 스크립트
# 서버에서 직접 실행: sudo bash quick-fix-nginx.sh

set -e

echo "Nginx IPv6 문제 해결 중..."

# Nginx 중지
sudo systemctl stop nginx 2>/dev/null || true

# 기본 설정 파일에서 IPv6 비활성화
if [ -f /etc/nginx/sites-enabled/default ]; then
    sudo sed -i 's/listen \[::\]:80 default_server;/# listen [::]:80 default_server;/g' /etc/nginx/sites-enabled/default
    sudo sed -i 's/listen \[::\]:443 ssl default_server;/# listen [::]:443 ssl default_server;/g' /etc/nginx/sites-enabled/default
fi

if [ -f /etc/nginx/sites-available/default ]; then
    sudo sed -i 's/listen \[::\]:80 default_server;/# listen [::]:80 default_server;/g' /etc/nginx/sites-available/default
    sudo sed -i 's/listen \[::\]:443 ssl default_server;/# listen [::]:443 ssl default_server;/g' /etc/nginx/sites-available/default
fi

# Nginx 설정 테스트
if sudo nginx -t; then
    echo "설정이 올바릅니다. Nginx 시작..."
    sudo systemctl start nginx
    echo "완료!"
else
    echo "설정 오류가 있습니다."
    sudo nginx -t
    exit 1
fi

