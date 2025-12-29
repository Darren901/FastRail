package com.example.fastrail.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if("OPTIONS".equals(request.getMethod())){
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("無效的token");
        }

        token = token.substring(7);

        if(!jwtUtil.validateToken(token)){
            throw new RuntimeException("token已過期或無效");
        }

        request.setAttribute("userEmail", jwtUtil.getEmailFromToken(token));

        return true;
    }
}
