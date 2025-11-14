package com.hlink.urlshortener.config;

import com.hlink.urlshortener.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // X-Frame-Options를 SAMEORIGIN으로 설정하여 iframe에서 표시 가능하도록 함
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/**", "/api/urls", "/api/qrcode/**", "/api/stats/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/swagger-ui-custom.css", "/swagger-ui-init.js").permitAll()
                // Swagger UI 정적 리소스 경로 (상대 경로로 요청되는 리소스들)
                .requestMatchers("/index.css", "/swagger-initializer.js", "/swagger-ui-bundle.js", "/swagger-ui-standalone-preset.js", "/swagger-ui.css").permitAll()
                .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**", "/api/swagger-resources/**", "/api/webjars/**").permitAll()
                .requestMatchers(request -> {
                    String method = request.getMethod();
                    // OPTIONS 요청은 CORS preflight이므로 허용
                    if ("OPTIONS".equals(method)) {
                        return true;
                    }
                    String path = request.getRequestURI();
                    // /{shortCode} 패턴 매칭: /api로 시작하지 않고, 경로가 짧은 경우 (단축 코드)
                    return !path.startsWith("/api") && path.length() > 1 && path.length() < 100;
                }).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/account-manager/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

