package com.example.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
        // Private constructor to prevent instantiation
    }

    @Configuration
    @Profile("dev")
    static class DevSecurityConfig {
        @Bean
        public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/**").permitAll() // Libera tudo no dev
                )
                .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    @Configuration
    @Profile("prod")
    static class ProdSecurityConfig {
        @Bean
        public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/login", "/error", "/css/**").permitAll()
                    .requestMatchers("/vehicles/**", "/vehicle/**").authenticated()
                    .requestMatchers("/cart/**", "/user/**").authenticated()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
                )
                .logout(logout -> logout.permitAll());
            return http.build();
        }
    }
}
