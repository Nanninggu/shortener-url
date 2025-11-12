#!/bin/bash

# Nginx 설치 문제 해결 스크립트

set -e

echo "=========================================="
echo "Nginx 설치 문제 해결"
echo "=========================================="

# 1. dpkg 잠금 해제
echo "[1/4] dpkg 잠금 해제..."
sudo dpkg --configure -a

# 2. nginx 중지
echo "[2/4] Nginx 중지..."
sudo systemctl stop nginx 2>/dev/null || true

# 3. 기존 nginx 설정 제거
echo "[3/4] 기존 Nginx 설정 정리..."
sudo rm -f /etc/nginx/sites-enabled/default
sudo rm -f /etc/nginx/sites-enabled/url-shortener

# 4. nginx 재설치
echo "[4/4] Nginx 재설치..."
sudo apt-get update
sudo apt-get install --reinstall -y nginx

echo ""
echo "=========================================="
echo "Nginx 문제 해결 완료!"
echo "=========================================="
echo ""
echo "이제 deploy.sh를 다시 실행하세요."

