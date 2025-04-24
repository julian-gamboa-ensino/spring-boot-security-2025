package com.example.ui.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String requestPath = request.getRequestURI();
        
        // Verifica se é uma rota que requer perfil de vendedor
        if (requestPath.startsWith("/users")) {
            HttpSession session = request.getSession(false);
            String role = (String) session.getAttribute("role");
            
            if (!"VENDEDOR".equals(role)) {
                response.sendRedirect("/vehicles");
                return false;
            }
        }
        
        return true;
    }
} 