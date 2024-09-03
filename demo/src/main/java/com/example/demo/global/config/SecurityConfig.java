package com.example.demo.global.config;

import com.example.demo.global.jwt.JwtFilter;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenRedisService tokenRedisService;
    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .httpBasic(httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.disable())
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .requestMatchers("/v1/user/signup").permitAll()
                            .requestMatchers("/v1/user/login").permitAll()
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    authorizationManagerRequestMatcherRegistry.anyRequest().authenticated();
                })
                .cors(Customizer.withDefaults())
                .sessionManagement(httpSecuritySessionManagementConfigurer
                        -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtProvider, tokenRedisService), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer
                        -> httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint( // 401 unauthorized
                        (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .build();
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class CorsFilter extends OncePerRequestFilter {
        // Cors 제외할 도메인 목록
        private static final List<String> allowedOrigins = Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5000"
        );

        @Override
        protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
        ) throws ServletException, IOException {
            String origin = request.getHeader("Origin");
            if (allowedOrigins.contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }

            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers",
                    "Origin, X-Requested-With, Content-Type, Accept, Authorization, x-xsrf-token");
            response.setHeader("Access-Control-Expose-Headers", "Accesstoken");

            // Handle CORS preflight request
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                filterChain.doFilter(request, response);
            }
        }

        @Bean
        public PasswordEn
    }


}
