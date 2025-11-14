#!/bin/bash

# Nginx 설정 파일만 업데이트하는 스크립트
# 사용법: 서버에서 실행

set -e

echo "=========================================="
echo "Nginx 설정 업데이트"
echo "=========================================="

APP_DIR="/opt/url-shortener"
BACKEND_DIR="$APP_DIR/springboot"

# Nginx 설정 파일 백업
if [ -f /etc/nginx/sites-available/url-shortener ]; then
    echo "[1/3] 기존 설정 파일 백업..."
    sudo cp /etc/nginx/sites-available/url-shortener /etc/nginx/sites-available/url-shortener.backup.$(date +%Y%m%d_%H%M%S)
    echo "백업 완료"
fi

# Nginx 설정 파일 복사
if [ -f "$BACKEND_DIR/nginx.conf" ]; then
    echo "[2/3] Nginx 설정 파일 복사..."
    sudo cp $BACKEND_DIR/nginx.conf /etc/nginx/sites-available/url-shortener
    echo "복사 완료"
else
    echo "오류: $BACKEND_DIR/nginx.conf 파일을 찾을 수 없습니다."
    exit 1
fi

# Nginx 설정 테스트 및 재시작
echo "[3/3] Nginx 설정 테스트 및 재시작..."
if sudo nginx -t; then
    echo "설정 테스트 성공"
    sudo systemctl reload nginx
    echo "Nginx 재시작 완료"
    echo ""
    echo "=========================================="
    echo "Nginx 설정 업데이트 완료!"
    echo "=========================================="
else
    echo "오류: Nginx 설정 테스트 실패"
    echo "백업 파일에서 복원하려면:"
    echo "sudo cp /etc/nginx/sites-available/url-shortener.backup.* /etc/nginx/sites-available/url-shortener"
    exit 1
fi

