package com.example.ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
        // Construtor privado para evitar instância
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        UserDetails vendedor = User.withUsername("vendedor")
            .password("{noop}vendedor123")
            .roles("VENDEDOR")
            .build();

        UserDetails cliente = User.withUsername("cliente")
            .password("{noop}cliente123")
            .roles("CLIENTE")
            .build();

        UserDetails admin = User.withUsername("admin")
            .password("{noop}admin123")
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(vendedor, cliente, admin);
    }

    @Configuration
    @Profile("dev")
    static class DevSecurityConfig {
        @Bean
        public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/users/**").hasRole("VENDEDOR")
                    .requestMatchers("/**").permitAll()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/vehicles")
                    .permitAll()
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
                    .requestMatchers("/users/**").hasRole("VENDEDOR")
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
