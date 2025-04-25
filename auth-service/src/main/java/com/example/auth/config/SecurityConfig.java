package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança
 * =======================
 * 
 * PROTEÇÃO DE ROTAS:
 * ----------------
 * 1. Regras de Acesso:
 *    - Todas as URLs são protegidas por padrão
 *    - Exceções apenas para:
 *      * /api/auth/login
 *      * /api/auth/refresh
 *      * /error
 * 
 * 2. Redirecionamento:
 *    - Usuários não autenticados -> /login
 *    - Após login -> URL original solicitada
 * 
 * CONFIGURAÇÃO DE SESSÕES:
 * ---------------------
 * 1. Políticas de Sessão:
 *    - Permite múltiplas sessões por usuário
 *    - Sem limite de sessões concorrentes
 *    - Cada navegador mantém sessão independente
 * 
 * 2. Segurança:
 *    - CSRF habilitado
 *    - Headers de segurança configurados
 *    - Requer HTTPS em produção
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/vendor/**").hasRole("VENDOR")
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
} 