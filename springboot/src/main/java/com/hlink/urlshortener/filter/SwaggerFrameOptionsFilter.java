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
 * Swagger UI 경로에 대해서는 X-Frame-Options 헤더를 설정하지 않고,
 * 다른 경로에 대해서는 SAMEORIGIN을 설정하는 필터
 */
@Component
@Order(100)
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
            // Swagger UI 경로: X-Frame-Options 헤더를 설정하지 않음
            // 응답 래퍼를 사용하여 헤더 설정을 차단
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
                @Override
                public void setHeader(String name, String value) {
                    // X-Frame-Options 헤더는 설정하지 않음
                    if (!"X-Frame-Options".equalsIgnoreCase(name)) {
                        super.setHeader(name, value);
                    }
                }

                @Override
                public void addHeader(String name, String value) {
                    // X-Frame-Options 헤더는 추가하지 않음
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
        } else {
            // 다른 경로: SAMEORIGIN 헤더 설정
            chain.doFilter(request, response);
            if (!response.isCommitted()) {
                response.setHeader("X-Frame-Options", "SAMEORIGIN");
            }
        }
    }
}

