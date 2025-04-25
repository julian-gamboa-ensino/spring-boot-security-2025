package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de Segurança
 * ======================
 * 
 * PROTEÇÕES:
 * --------
 * 1. Dados Sensíveis:
 *    - CPF criptografado
 *    - Nome criptografado
 *    - Login criptografado
 *    - Senha com hash BCrypt
 * 
 * 2. APIs:
 *    - Spring Security em endpoints
 *    - Autenticação JWT
 *    - Autorização por perfil
 * 
 * BANCO DE DADOS:
 * ------------
 * - MySQL com InnoDB
 * - Transações ACID
 * - Volumes Docker persistentes
 * 
 * TESTES:
 * ------
 * - TDD com Cypress (front)
 * - JUnit (back)
 * - Testes de integração
 * - Documentação clara
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