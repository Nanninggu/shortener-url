#!/bin/bash

# 전체 기능 테스트 스크립트
# 결과를 파일로 저장

BASE_URL="http://localhost:8080/api"
TEST_RESULTS_FILE="/tmp/feature-test-results.txt"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

echo "=========================================" > $TEST_RESULTS_FILE
echo "H-Link 기능 테스트 결과" >> $TEST_RESULTS_FILE
echo "테스트 시작 시간: $TIMESTAMP" >> $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE
echo "" >> $TEST_RESULTS_FILE

# 1. 로그인 테스트
echo "[1] 로그인 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

echo "응답: $LOGIN_RESPONSE" >> $TEST_RESULTS_FILE

# 토큰 추출
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ 로그인 실패!" >> $TEST_RESULTS_FILE
  echo "테스트를 계속할 수 없습니다." >> $TEST_RESULTS_FILE
  cat $TEST_RESULTS_FILE
  exit 1
else
  echo "✅ 로그인 성공 (토큰: ${TOKEN:0:20}...)" >> $TEST_RESULTS_FILE
  echo "Authorization: Bearer $TOKEN" > /tmp/auth_token.txt
fi
echo "" >> $TEST_RESULTS_FILE

# 2. 홈 - URL 단축 생성 테스트
echo "[2] 홈 - URL 단축 생성 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
# 고유한 URL 생성 (타임스탬프 사용)
UNIQUE_URL="https://www.example.com/test-$(date +%s)"
URL_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/urls" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"originalUrl\":\"$UNIQUE_URL\",\"expirationDays\":30}")

echo "응답: $URL_CREATE_RESPONSE" >> $TEST_RESULTS_FILE
SHORT_CODE=$(echo $URL_CREATE_RESPONSE | grep -o '"shortCode":"[^"]*' | cut -d'"' -f4)
if [ ! -z "$SHORT_CODE" ]; then
  echo "✅ URL 단축 생성 성공 (코드: $SHORT_CODE)" >> $TEST_RESULTS_FILE
  echo "$SHORT_CODE" > /tmp/test_short_code.txt
else
  echo "❌ URL 단축 생성 실패" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 3. URL 목록 조회 테스트
echo "[3] URL 목록 조회 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
URL_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/urls?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "응답: $URL_LIST_RESPONSE" >> $TEST_RESULTS_FILE
if echo "$URL_LIST_RESPONSE" | grep -q "shortCode"; then
  echo "✅ URL 목록 조회 성공" >> $TEST_RESULTS_FILE
else
  echo "❌ URL 목록 조회 실패" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 4. 대량 생성 테스트
echo "[4] 대량 생성 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
# 고유한 URL 생성 (타임스탬프 사용)
TIMESTAMP=$(date +%s)
BULK_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/urls/bulk" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"urls\":[{\"originalUrl\":\"https://www.example1.com/test-$TIMESTAMP\"},{\"originalUrl\":\"https://www.example2.com/test-$TIMESTAMP\"}]}")

echo "응답: $BULK_CREATE_RESPONSE" >> $TEST_RESULTS_FILE
if echo "$BULK_CREATE_RESPONSE" | grep -q "shortCode"; then
  echo "✅ 대량 생성 성공" >> $TEST_RESULTS_FILE
else
  echo "❌ 대량 생성 실패" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 5. 지원 티켓 생성 테스트
echo "[5] 지원 티켓 생성 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
# 먼저 사용자 ID 가져오기
USER_INFO=$(curl -s -X GET "$BASE_URL/users/by-username/admin" \
  -H "Authorization: Bearer $TOKEN")
USER_ID=$(echo $USER_INFO | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ ! -z "$USER_ID" ]; then
  TICKET_RESPONSE=$(curl -s -X POST "$BASE_URL/support/tickets?userId=$USER_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"subject":"테스트 티켓","description":"기능 테스트용 티켓입니다","priority":"MEDIUM"}')
  
  echo "응답: $TICKET_RESPONSE" >> $TEST_RESULTS_FILE
  TICKET_ID=$(echo $TICKET_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  if [ ! -z "$TICKET_ID" ]; then
    echo "✅ 지원 티켓 생성 성공 (ID: $TICKET_ID)" >> $TEST_RESULTS_FILE
    echo "$TICKET_ID" > /tmp/test_ticket_id.txt
  else
    echo "❌ 지원 티켓 생성 실패" >> $TEST_RESULTS_FILE
  fi
else
  echo "❌ 사용자 ID를 가져올 수 없음" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 6. 팀 생성 테스트
echo "[6] 팀 생성 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
if [ ! -z "$USER_ID" ]; then
  TEAM_RESPONSE=$(curl -s -X POST "$BASE_URL/teams?ownerId=$USER_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"테스트 팀"}')
  
  echo "응답: $TEAM_RESPONSE" >> $TEST_RESULTS_FILE
  TEAM_ID=$(echo $TEAM_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  if [ ! -z "$TEAM_ID" ]; then
    echo "✅ 팀 생성 성공 (ID: $TEAM_ID)" >> $TEST_RESULTS_FILE
    echo "$TEAM_ID" > /tmp/test_team_id.txt
  else
    echo "❌ 팀 생성 실패" >> $TEST_RESULTS_FILE
  fi
else
  echo "❌ 사용자 ID를 가져올 수 없음" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 7. API 키 생성 테스트
echo "[7] API 키 생성 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
if [ ! -z "$USER_ID" ]; then
  # 한글은 인코딩 문제가 있을 수 있으므로 영문으로 테스트
  API_KEY_RESPONSE=$(curl -s -X POST "$BASE_URL/api-keys?keyName=TestKey&userId=$USER_ID" \
    -H "Authorization: Bearer $TOKEN")
  
  echo "응답: $API_KEY_RESPONSE" >> $TEST_RESULTS_FILE
  API_KEY_ID=$(echo $API_KEY_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
  if [ ! -z "$API_KEY_ID" ]; then
    echo "✅ API 키 생성 성공 (ID: $API_KEY_ID)" >> $TEST_RESULTS_FILE
    echo "$API_KEY_ID" > /tmp/test_api_key_id.txt
  else
    echo "❌ API 키 생성 실패" >> $TEST_RESULTS_FILE
  fi
else
  echo "❌ 사용자 ID를 가져올 수 없음" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 8. 화이트라벨 설정 테스트
echo "[8] 화이트라벨 설정 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
if [ ! -z "$USER_ID" ]; then
  WHITELABEL_RESPONSE=$(curl -s -X POST "$BASE_URL/white-label?userId=$USER_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"brandName":"테스트 브랜드","primaryColor":"#007bff","logoUrl":"https://example.com/logo.png"}')
  
  echo "응답: $WHITELABEL_RESPONSE" >> $TEST_RESULTS_FILE
  if echo "$WHITELABEL_RESPONSE" | grep -q "brandName"; then
    echo "✅ 화이트라벨 설정 성공" >> $TEST_RESULTS_FILE
  else
    echo "❌ 화이트라벨 설정 실패" >> $TEST_RESULTS_FILE
  fi
else
  echo "❌ 사용자 ID를 가져올 수 없음" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 9. 관리자 통계 조회 테스트
echo "[9] 관리자 통계 조회 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
ADMIN_STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/admin/stats" \
  -H "Authorization: Bearer $TOKEN")

echo "응답: $ADMIN_STATS_RESPONSE" >> $TEST_RESULTS_FILE
if echo "$ADMIN_STATS_RESPONSE" | grep -q "totalUrls\|totalUsers"; then
  echo "✅ 관리자 통계 조회 성공" >> $TEST_RESULTS_FILE
else
  echo "❌ 관리자 통계 조회 실패" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

# 10. API 문서 접근 테스트
echo "[10] API 문서 접근 테스트" >> $TEST_RESULTS_FILE
echo "-------------------" >> $TEST_RESULTS_FILE
API_DOCS_RESPONSE=$(curl -s -X GET "http://localhost:8080/v3/api-docs" \
  -H "Authorization: Bearer $TOKEN")

if echo "$API_DOCS_RESPONSE" | grep -q "openapi\|swagger"; then
  echo "✅ API 문서 접근 성공" >> $TEST_RESULTS_FILE
else
  echo "❌ API 문서 접근 실패" >> $TEST_RESULTS_FILE
fi
echo "" >> $TEST_RESULTS_FILE

echo "=========================================" >> $TEST_RESULTS_FILE
echo "테스트 완료 시간: $(date '+%Y-%m-%d %H:%M:%S')" >> $TEST_RESULTS_FILE
echo "=========================================" >> $TEST_RESULTS_FILE

# 결과 출력
cat $TEST_RESULTS_FILE

