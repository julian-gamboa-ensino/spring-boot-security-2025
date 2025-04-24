package com.example.ui.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/login", "/register", "/css/", "/js/", "/images/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String requestPath = request.getRequestURI();
        
        // Permite acesso a caminhos públicos
        if (isPublicPath(requestPath)) {
            return true;
        }

        // Verifica se existe uma sessão com token
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("token") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
} 