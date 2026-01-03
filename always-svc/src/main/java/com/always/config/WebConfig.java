package com.always.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8088", "http://192.168.75.207:8088") // Vue 개발 서버
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 (모든 요청)
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");
        
        // JWT 인증 인터셉터 (인증이 필요한 요청)
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/hello", "/api/menus/**", "/api/openai/**"); // 인증 불필요한 경로 제외
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 이미지 파일을 제공하기 위한 정적 리소스 핸들러
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:C:/Users/jy_kim/Pictures/server_picture/");
    }
}



