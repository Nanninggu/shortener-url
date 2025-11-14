package com.hlink.urlshortener.config;

import com.hlink.urlshortener.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // API 엔드포인트: 특정 origin만 허용 (credentials 필요)
        // Local: localhost, Prod: NCP VM IP
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000", "http://localhost:3001", "http://localhost:5173",  // Local
                    "http://223.130.157.227:3000", "http://223.130.157.227",  // Prod
                    "http://49.50.138.63", "https://49.50.138.63"  // Legacy
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // preflight 캐시 시간 1시간
        
        // 리다이렉트 엔드포인트: 모든 origin 허용 (credentials 불필요)
        registry.addMapping("/{shortCode}")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI 커스텀 CSS 및 JS 파일 제공
        registry.addResourceHandler("/swagger-ui-custom.css", "/swagger-ui-init.js")
                .addResourceLocations("classpath:/static/");
    }
}

