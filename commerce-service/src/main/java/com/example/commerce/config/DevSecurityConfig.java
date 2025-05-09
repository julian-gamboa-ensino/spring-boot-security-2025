package com.example.commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
@EnableWebSecurity
public class DevSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if ("dev".equals(System.getProperty("spring.profiles.active"))) {
            http
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        } else {
            http
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable());
        }
            
        return http.build();
    }
}
