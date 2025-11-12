#!/bin/bash

# 빠른 배포 스크립트 (서버에서 직접 실행)
# 사용법: 서버에 접속 후 이 스크립트를 실행

set -e

echo "=========================================="
echo "빠른 배포 시작"
echo "=========================================="

APP_DIR="/opt/url-shortener"
BACKEND_DIR="$APP_DIR/springboot"
FRONTEND_DIR="$APP_DIR/vue"

# 1. 백엔드 재빌드 및 재시작
echo "[1/3] 백엔드 재빌드..."
cd $BACKEND_DIR
mvn clean package -DskipTests
sudo systemctl restart url-shortener
echo "백엔드 재시작 완료"

# 2. 프론트엔드 재빌드
echo "[2/3] 프론트엔드 재빌드..."
cd $FRONTEND_DIR
npm install
npm run build
echo "프론트엔드 빌드 완료"

# 3. Nginx 재시작
echo "[3/3] Nginx 재시작..."
sudo systemctl restart nginx
echo "Nginx 재시작 완료"

echo ""
echo "=========================================="
echo "배포 완료!"
echo "=========================================="
echo ""
echo "서비스 상태:"
sudo systemctl status url-shortener --no-pager -l
echo ""
echo "로그 확인: sudo journalctl -u url-shortener -f"

