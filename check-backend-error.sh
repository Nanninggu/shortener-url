#!/bin/bash

# 백엔드 500 에러 진단 스크립트
# 사용법: 서버에서 sudo bash check-backend-error.sh 실행

echo "=========================================="
echo "백엔드 500 에러 진단"
echo "=========================================="
echo ""

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 1. 백엔드 서비스 상태
echo "=== [1/5] 백엔드 서비스 상태 ==="
if systemctl is-active --quiet url-shortener; then
    echo -e "${GREEN}✓ 백엔드 서비스 실행 중${NC}"
else
    echo -e "${RED}✗ 백엔드 서비스 중지됨${NC}"
    echo "서비스 시작: sudo systemctl start url-shortener"
fi
echo ""

# 2. 최근 백엔드 로그 확인
echo "=== [2/5] 최근 백엔드 로그 (에러 포함) ==="
echo "최근 50줄의 로그:"
sudo journalctl -u url-shortener -n 50 --no-pager | grep -A 5 -B 5 -i "error\|exception\|failed" || sudo journalctl -u url-shortener -n 50 --no-pager
echo ""

# 3. H2 데이터베이스 확인
echo "=== [3/5] H2 데이터베이스 확인 ==="

# H2 데이터베이스 디렉토리 확인
H2_DATA_DIR="/opt/url-shortener/data"
if [ -d "$H2_DATA_DIR" ]; then
    echo -e "${GREEN}✓ H2 데이터베이스 디렉토리 존재${NC}"
    DB_FILES=$(find "$H2_DATA_DIR" -name "*.mv.db" -o -name "*.trace.db" 2>/dev/null | wc -l)
    echo "H2 데이터베이스 파일 수: $DB_FILES"
    
    if [ "$DB_FILES" -gt 0 ]; then
        echo -e "${GREEN}✓ H2 데이터베이스 파일 존재${NC}"
    else
        echo -e "${YELLOW}⚠ H2 데이터베이스 파일이 아직 생성되지 않았습니다. (애플리케이션 시작 시 자동 생성)${NC}"
    fi
else
    echo -e "${YELLOW}⚠ H2 데이터베이스 디렉토리가 없습니다. (애플리케이션 시작 시 자동 생성)${NC}"
fi

# H2 TCP 포트 확인
if netstat -tlnp 2>/dev/null | grep -q ":9092 " || ss -tlnp 2>/dev/null | grep -q ":9092 "; then
    echo -e "${GREEN}✓ H2 TCP 서버 실행 중 (포트 9092)${NC}"
else
    echo -e "${YELLOW}⚠ H2 TCP 서버가 실행되지 않았습니다. (애플리케이션 시작 시 자동 시작)${NC}"
fi
echo ""

# 4. 환경 변수 확인
echo "=== [4/5] 환경 변수 확인 ==="
echo "서비스 파일의 환경 변수:"
sudo cat /etc/systemd/system/url-shortener.service | grep -E "Environment=" || echo "환경 변수 없음"
echo ""

# 5. API 직접 테스트
echo "=== [5/5] API 직접 테스트 ==="
echo "헬스체크:"
curl -s http://localhost:8080/actuator/health | head -20 || echo "백엔드 응답 없음"
echo ""
echo ""

# 추가 진단 정보
echo "=========================================="
echo "추가 진단 정보"
echo "=========================================="
echo ""

# JAR 파일 확인
JAR_FILE="/opt/url-shortener/springboot/target/url-shortener-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    echo -e "${GREEN}✓ JAR 파일 존재${NC}"
    ls -lh "$JAR_FILE"
else
    echo -e "${RED}✗ JAR 파일 없음${NC}"
fi
echo ""

# 포트 확인
echo "포트 8080 상태:"
netstat -tlnp 2>/dev/null | grep ":8080 " || ss -tlnp 2>/dev/null | grep ":8080 " || echo "포트 8080에서 실행 중인 프로세스 없음"
echo ""

# 최근 에러 로그 상세
echo "최근 에러 로그 상세 (마지막 100줄):"
sudo journalctl -u url-shortener -n 100 --no-pager | tail -30
echo ""

echo "=========================================="
echo "진단 완료"
echo "=========================================="
echo ""
echo "문제 해결 방법:"
echo ""
echo "1. 데이터베이스 테이블이 없는 경우:"
echo "   sudo -u postgres psql -d url_shortener -f /opt/url-shortener/springboot/src/main/resources/db/schema.sql"
echo ""
echo "2. 데이터베이스 연결 문제:"
echo "   sudo cat /etc/systemd/system/url-shortener.service | grep DB_PASSWORD"
echo "   # 비밀번호 확인 후 서비스 재시작"
echo ""
echo "3. 서비스 재시작:"
echo "   sudo systemctl restart url-shortener"
echo "   sudo journalctl -u url-shortener -f"
echo ""

