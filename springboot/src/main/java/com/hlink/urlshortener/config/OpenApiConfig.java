package com.hlink.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("H-Link URL Shortener API")
                        .version("1.0.0")
                        .description("""
                            H-Link URL Shortener API 문서입니다.
                            
                            이 API는 URL 단축 서비스를 제공하며, 다음 기능을 포함합니다:
                            - URL 단축 및 관리
                            - 사용자 인증 및 권한 관리
                            - 통계 및 분석
                            - 팀 관리
                            - 화이트라벨 설정
                            - 지원 티켓 관리
                            
                            대부분의 엔드포인트는 JWT 인증이 필요합니다.
                            """)
                        .contact(new Contact()
                                .name("H-Link Team")
                                .email("support@hlink.com")
                                .url("https://hlink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("http://49.50.138.63")
                                .description("프로덕션 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요. 형식: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}

