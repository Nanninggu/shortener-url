package com.hlink.urlshortener.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Swagger UI 경로에 대해서만 X-Frame-Options 헤더를 제거하여
 * 다른 포트에서 iframe으로 접근할 수 있도록 허용하는 필터
 * 
 * Spring Security 필터 체인 이후에 실행되어 헤더를 제거합니다.
 */
@Component
@Order(100) // Spring Security 필터 체인 이후에 실행
public class SwaggerFrameOptionsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // Swagger UI 관련 경로인지 확인
        if (path != null && (path.startsWith("/swagger-ui") 
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars"))) {
            // X-Frame-Options 헤더 제거를 위한 커스텀 응답 래퍼 사용
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
                @Override
                public void setHeader(String name, String value) {
                    // X-Frame-Options 헤더는 설정하지 않음 (제거)
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setHeader(name, value);
                    }
                }

                @Override
                public void addHeader(String name, String value) {
                    // X-Frame-Options 헤더는 추가하지 않음 (제거)
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.addHeader(name, value);
                    }
                }

                @Override
                public void setDateHeader(String name, long date) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setDateHeader(name, date);
                    }
                }

                @Override
                public void addDateHeader(String name, long date) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.addDateHeader(name, date);
                    }
                }

                @Override
                public void setIntHeader(String name, int value) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setIntHeader(name, value);
                    }
                }

                @Override
                public void addIntHeader(String name, int value) {
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.addIntHeader(name, value);
                    }
                }
            };

            chain.doFilter(request, wrappedResponse);
            
            // 응답이 커밋되기 전에 X-Frame-Options 헤더를 명시적으로 제거
            // Spring Security가 설정한 헤더를 제거하기 위해 헤더를 덮어씀
            if (!response.isCommitted()) {
                // 헤더를 빈 문자열로 설정하여 제거
                response.setHeader("X-Frame-Options", "");
                // 또는 헤더를 완전히 제거하려면 (하지만 setHeader로는 완전히 제거 불가)
                // 대신 ALLOWALL로 설정하여 모든 origin에서 접근 가능하도록 함
                // 하지만 브라우저가 이를 지원하지 않으므로 헤더를 제거하는 것이 더 나음
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}

