package com.example.fastrail.config;

import com.example.fastrail.util.JwtAuthInterceptor;
import com.example.fastrail.util.SessionAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final SessionAuthInterceptor sessionAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/users/register",
                        "/api/users/login",
                        "/api/users/send-otp",
                        "/api/google",
                        "/api/google/**",
                        "/api/backstage/**",
                        "/favicon.ico",
                        "/error"
                );

        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/api/backstage/**")
                .excludePathPatterns("/api/backstage/admin/login");
    }
}
