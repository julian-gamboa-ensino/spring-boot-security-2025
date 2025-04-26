package com.example.commerce.security;


import jakarta.annotation.Nonnull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, 
    @Nonnull HttpServletResponse response, 
    @Nonnull Object handler) {
        String roles = request.getHeader("X-User-Roles");
        String requestPath = request.getRequestURI();
        
        // Verificar permissões baseado no path e roles
        if (requestPath.startsWith("/api/admin") && !roles.contains("ROLE_ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        
        return true;
    }
} 