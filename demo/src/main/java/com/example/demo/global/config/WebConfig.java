package com.example.demo.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 스프링 부트 애플리케이션에서 정적 리소스(예: 이미지, CSS 파일, JavaScript 파일 등)를 제공하는 방법을 정의
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/");
    }
}
