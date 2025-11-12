# SSL 인증서 설정 가이드

H-Link URL Shortener 애플리케이션에 SSL/TLS 인증서를 설정하는 방법을 안내합니다.

## 1. Let's Encrypt를 사용한 무료 SSL 인증서

### Certbot 설치 (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install certbot
```

### Nginx와 함께 사용하는 경우

```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### Standalone 모드 (Spring Boot 직접 사용)

```bash
sudo certbot certonly --standalone -d yourdomain.com
```

인증서는 다음 위치에 저장됩니다:
- `/etc/letsencrypt/live/yourdomain.com/fullchain.pem`
- `/etc/letsencrypt/live/yourdomain.com/privkey.pem`

## 2. Spring Boot에 SSL 설정

### application.yml 설정

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: /etc/letsencrypt/live/yourdomain.com/keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
    key-alias: tomcat
```

### Let's Encrypt 인증서를 PKCS12 형식으로 변환

```bash
sudo openssl pkcs12 -export -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/yourdomain.com/privkey.pem \
  -out /etc/letsencrypt/live/yourdomain.com/keystore.p12 \
  -name tomcat \
  -password pass:your-password
```

## 3. Nginx를 리버스 프록시로 사용 (권장)

### Nginx 설정 예시

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 4. 자동 갱신 설정

Let's Encrypt 인증서는 90일마다 갱신해야 합니다.

### Cron 작업 추가

```bash
sudo crontab -e
```

다음 줄 추가:
```
0 0 * * * certbot renew --quiet && systemctl reload nginx
```

## 5. 보안 헤더 설정

### Spring Security 설정 추가

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
            );
        return http.build();
    }
}
```

## 6. SSL 테스트

다음 도구로 SSL 설정을 테스트할 수 있습니다:
- [SSL Labs SSL Test](https://www.ssllabs.com/ssltest/)
- [SSL Checker](https://www.sslshopper.com/ssl-checker.html)

## 7. 추가 보안 권장사항

1. **HSTS (HTTP Strict Transport Security)** 활성화
2. **TLS 1.2 이상** 사용
3. **강력한 암호화 알고리즘** 사용
4. **정기적인 인증서 갱신** 확인
5. **보안 헤더** 설정 (X-Frame-Options, X-Content-Type-Options 등)

## 문제 해결

### 인증서 갱신 실패
```bash
sudo certbot renew --dry-run
```

### 포트 충돌
Spring Boot와 Nginx가 같은 포트를 사용하지 않도록 확인하세요.

### 권한 문제
인증서 파일에 적절한 읽기 권한이 있는지 확인하세요.

