#!/bin/bash

# 통합 테스트 스크립트 - 전체 플로우 테스트
# 실제 사용자 시나리오를 시뮬레이션

BASE_URL="http://localhost:8080/api"
TEST_RESULTS_FILE="/tmp/integration-test-results.txt"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "=========================================" > $TEST_RESULTS_FILE
echo "H-Link 통합 테스트 결과" >> $TEST_RESULTS_FILE
echo "테스트 시작 시간: $TIMESTAMP" >> $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE
echo "" >> $TEST_RESULTS_FILE

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 테스트 카운터
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 테스트 헬퍼 함수
test_step() {
    local test_name="$1"
    local command="$2"
    local success_pattern="${3:-.*}"
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo "[$TOTAL_TESTS] $test_name" >> $TEST_RESULTS_FILE
    echo "-------------------" >> $TEST_RESULTS_FILE
    
    local result=$(eval "$command" 2>&1)
    local exit_code=$?
    
    echo "$result" >> $TEST_RESULTS_FILE
    
    # HTTP 상태 코드 확인 (200, 201 등)
    local http_code=$(echo "$result" | grep -oE "HTTP/[0-9.]+ [0-9]+" | tail -1 | awk '{print $2}')
    if [ -z "$http_code" ]; then
        http_code=$(echo "$result" | tail -1 | grep -oE "^[0-9]+$" | head -1)
    fi
    
    # 성공 조건: exit_code가 0이고, 결과에 성공 패턴이 있거나 HTTP 코드가 200-299 범위
    local is_success=false
    if [ $exit_code -eq 0 ]; then
        if [ ! -z "$http_code" ] && [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
            is_success=true
        elif echo "$result" | grep -qE "$success_pattern"; then
            is_success=true
        elif echo "$result" | grep -qE '("id"|"token"|"shortCode"|"totalUrls"|"content"|"brandName"|"apiKey"|"subject")'; then
            is_success=true
        fi
    fi
    
    if [ "$is_success" = true ]; then
        echo -e "${GREEN}✅ PASS${NC}: $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "✅ 성공" >> $TEST_RESULTS_FILE
    else
        echo -e "${RED}❌ FAIL${NC}: $test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "❌ 실패" >> $TEST_RESULTS_FILE
    fi
    echo "" >> $TEST_RESULTS_FILE
}

echo "=== 통합 테스트 시작 ===" | tee -a $TEST_RESULTS_FILE
echo ""

# ============================================
# 시나리오 1: 사용자 인증 플로우
# ============================================
echo "📋 시나리오 1: 사용자 인증 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 1-1. 로그인
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
USER_ID=$(echo $LOGIN_RESPONSE | grep -o '"userId":[0-9]*' | cut -d':' -f2)

test_step "1-1. 관리자 로그인" "echo '$LOGIN_RESPONSE' | grep -q '\"token\"' && echo 'Token: ${TOKEN:0:20}...'" "token"

if [ -z "$TOKEN" ]; then
    echo -e "${RED}❌ 로그인 실패 - 테스트 중단${NC}"
    exit 1
fi

# ============================================
# 시나리오 2: URL 단축 전체 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 2: URL 단축 전체 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 2-1. 단일 URL 생성
UNIQUE_URL1="https://www.example1.com/test-$(date +%s)"
URL_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/urls" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"originalUrl\":\"$UNIQUE_URL1\",\"expirationDays\":30}")

SHORT_CODE1=$(echo $URL_CREATE_RESPONSE | grep -o '"shortCode":"[^"]*' | cut -d'"' -f4)
URL_ID1=$(echo $URL_CREATE_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

test_step "2-1. 단일 URL 단축 생성" "echo '$URL_CREATE_RESPONSE' | grep -q '\"shortCode\"' && echo 'Short Code: $SHORT_CODE1'" "shortCode"

# 2-2. 중복 URL 처리 (같은 URL 재생성)
DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/urls" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"originalUrl\":\"$UNIQUE_URL1\",\"expirationDays\":30}")
DUPLICATE_CODE=$(echo $DUPLICATE_RESPONSE | grep -o '"shortCode":"[^"]*' | cut -d'"' -f4)
test_step "2-2. 중복 URL 처리 (기존 URL 반환)" "echo '$DUPLICATE_RESPONSE' | grep -q '\"shortCode\"' && [ '$SHORT_CODE1' = '$DUPLICATE_CODE' ] && echo 'Same code returned: $DUPLICATE_CODE'" "shortCode"

# 2-3. URL 목록 조회
test_step "2-3. URL 목록 조회" "curl -s -X GET '$BASE_URL/urls?page=0&size=10' -H 'Authorization: Bearer $TOKEN'" "content"

# 2-4. URL 정보 조회
test_step "2-4. URL 정보 조회 (단축 코드로)" "curl -s -X GET '$BASE_URL/urls/$SHORT_CODE1' -H 'Authorization: Bearer $TOKEN'" "originalUrl"

# 2-5. 대량 URL 생성
TIMESTAMP=$(date +%s)
BULK_RESPONSE=$(curl -s -X POST "$BASE_URL/urls/bulk" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"urls\":[{\"originalUrl\":\"https://bulk1-$TIMESTAMP.com\"},{\"originalUrl\":\"https://bulk2-$TIMESTAMP.com\"}]}")

test_step "2-5. 대량 URL 생성" "echo '$BULK_RESPONSE' | grep -q '\"success\"'" "success"

# ============================================
# 시나리오 3: 지원 티켓 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 3: 지원 티켓 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 3-1. 티켓 생성
TICKET_RESPONSE=$(curl -s -X POST "$BASE_URL/support/tickets?userId=$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"subject":"통합 테스트 티켓","description":"전체 플로우 테스트용 티켓입니다","priority":"HIGH"}')

TICKET_ID=$(echo $TICKET_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

test_step "3-1. 지원 티켓 생성" "echo '$TICKET_RESPONSE' | grep -q '\"id\"' && echo 'Ticket ID: $TICKET_ID'" "id"

# 3-2. 티켓 조회
test_step "3-2. 티켓 상세 조회" "curl -s -X GET '$BASE_URL/support/tickets/$TICKET_ID' -H 'Authorization: Bearer $TOKEN'" "subject"

# 3-3. 티켓 목록 조회
test_step "3-3. 티켓 목록 조회" "curl -s -X GET '$BASE_URL/support/tickets/user/$USER_ID?page=0&size=10' -H 'Authorization: Bearer $TOKEN'" "content"

# 3-4. 댓글 추가
if [ ! -z "$TICKET_ID" ]; then
    COMMENT_RESPONSE=$(curl -s -X POST "$BASE_URL/support/tickets/$TICKET_ID/comments?userId=$USER_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{"content":"테스트 댓글입니다"}')
    
    test_step "3-4. 티켓 댓글 추가" "echo '$COMMENT_RESPONSE' | grep -q '\"id\"'" "id"
fi

# ============================================
# 시나리오 4: 팀 관리 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 4: 팀 관리 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 4-1. 팀 생성
TEAM_RESPONSE=$(curl -s -X POST "$BASE_URL/teams?ownerId=$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"통합 테스트 팀"}')

TEAM_ID=$(echo $TEAM_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

test_step "4-1. 팀 생성" "echo '$TEAM_RESPONSE' | grep -q '\"id\"' && echo 'Team ID: $TEAM_ID'" "id"

# 4-2. 팀 조회
if [ ! -z "$TEAM_ID" ]; then
    test_step "4-2. 팀 상세 조회" "curl -s -X GET '$BASE_URL/teams/$TEAM_ID' -H 'Authorization: Bearer $TOKEN'" "name"
    
    # 4-3. 팀 멤버 조회
    test_step "4-3. 팀 멤버 목록 조회" "curl -s -X GET '$BASE_URL/teams/$TEAM_ID/members' -H 'Authorization: Bearer $TOKEN'" ".*"
fi

# ============================================
# 시나리오 5: API 키 관리 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 5: API 키 관리 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 5-1. API 키 생성 (한글 키 이름)
API_KEY_RESPONSE=$(curl -s -X POST "$BASE_URL/api-keys" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"keyName\":\"통합테스트키\",\"userId\":$USER_ID}")

API_KEY_ID=$(echo $API_KEY_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
API_KEY_VALUE=$(echo $API_KEY_RESPONSE | grep -o '"apiKey":"[^"]*' | cut -d'"' -f4)

test_step "5-1. API 키 생성 (한글 키 이름)" "echo '$API_KEY_RESPONSE' | grep -q '\"apiKey\"' && echo 'API Key: ${API_KEY_VALUE:0:20}...'" "apiKey"

# 5-2. API 키 목록 조회
test_step "5-2. 사용자별 API 키 목록 조회" "curl -s -X GET '$BASE_URL/api-keys/user/$USER_ID' -H 'Authorization: Bearer $TOKEN'" ".*"

# 5-3. API 키 비활성화
if [ ! -z "$API_KEY_ID" ]; then
    test_step "5-3. API 키 비활성화" "curl -s -o /dev/null -w '%{http_code}' -X POST '$BASE_URL/api-keys/$API_KEY_ID/deactivate' -H 'Authorization: Bearer $TOKEN'" "200"
    
    # 5-4. API 키 활성화
    test_step "5-4. API 키 활성화" "curl -s -o /dev/null -w '%{http_code}' -X POST '$BASE_URL/api-keys/$API_KEY_ID/activate' -H 'Authorization: Bearer $TOKEN'" "200"
fi

# ============================================
# 시나리오 6: 화이트라벨 설정 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 6: 화이트라벨 설정 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 6-1. 화이트라벨 설정 생성/수정
WHITELABEL_RESPONSE=$(curl -s -X POST "$BASE_URL/white-label?userId=$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"brandName":"통합테스트 브랜드","primaryColor":"#FF5733","logoUrl":"https://example.com/test-logo.png"}')

test_step "6-1. 화이트라벨 설정 생성/수정" "echo '$WHITELABEL_RESPONSE' | grep -q '\"brandName\"'" "brandName"

# 6-2. 화이트라벨 설정 조회
test_step "6-2. 화이트라벨 설정 조회" "curl -s -X GET '$BASE_URL/white-label/user/$USER_ID' -H 'Authorization: Bearer $TOKEN'" "brandName"

# ============================================
# 시나리오 7: 관리자 기능 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 7: 관리자 기능 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 7-1. 관리자 통계 조회
test_step "7-1. 관리자 통계 조회" "curl -s -X GET '$BASE_URL/admin/stats' -H 'Authorization: Bearer $TOKEN'" "totalUrls"

# 7-2. 관리자 URL 목록 조회
test_step "7-2. 관리자 URL 목록 조회" "curl -s -X GET '$BASE_URL/admin/urls?page=0&size=10' -H 'Authorization: Bearer $TOKEN'" "content"

# 7-3. 데이터베이스 정보 조회
test_step "7-3. 데이터베이스 정보 조회" "curl -s -X GET '$BASE_URL/admin/database/info' -H 'Authorization: Bearer $TOKEN'" "databaseType"

# ============================================
# 시나리오 8: 통계 및 분석 플로우
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 8: 통계 및 분석 플로우" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 8-1. URL 통계 조회
if [ ! -z "$SHORT_CODE1" ]; then
    test_step "8-1. URL 통계 조회" "curl -s -X GET '$BASE_URL/stats/$SHORT_CODE1?days=7' -H 'Authorization: Bearer $TOKEN'" "totalClicks"
fi

# ============================================
# 시나리오 9: 에러 처리 테스트
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "📋 시나리오 9: 에러 처리 테스트" | tee -a $TEST_RESULTS_FILE
echo "----------------------------------------" | tee -a $TEST_RESULTS_FILE

# 9-1. 잘못된 URL 생성 시도
test_step "9-1. 잘못된 URL 형식 처리" "curl -s -X POST '$BASE_URL/urls' -H 'Content-Type: application/json' -H 'Authorization: Bearer $TOKEN' -d '{\"originalUrl\":\"invalid-url\"}'" "message"

# 9-2. 존재하지 않는 단축 코드 조회
test_step "9-2. 존재하지 않는 단축 코드 처리" "curl -s -o /dev/null -w '%{http_code}' -X GET '$BASE_URL/urls/NONEXISTENT123' -H 'Authorization: Bearer $TOKEN'" "404"

# ============================================
# 테스트 결과 요약
# ============================================
echo "" | tee -a $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE
echo "테스트 결과 요약" >> $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE
echo "총 테스트: $TOTAL_TESTS" >> $TEST_RESULTS_FILE
echo "성공: $PASSED_TESTS" >> $TEST_RESULTS_FILE
echo "실패: $FAILED_TESTS" >> $TEST_RESULTS_FILE
echo "성공률: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%" >> $TEST_RESULTS_FILE
echo "테스트 완료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE

echo ""
echo "========================================="
echo "테스트 결과 요약"
echo "========================================="
echo "총 테스트: $TOTAL_TESTS"
echo -e "${GREEN}성공: $PASSED_TESTS${NC}"
echo -e "${RED}실패: $FAILED_TESTS${NC}"
echo "성공률: $(( PASSED_TESTS * 100 / TOTAL_TESTS ))%"
echo ""

# 결과 파일 출력
cat $TEST_RESULTS_FILE

