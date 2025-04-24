package com.example.ui.service;

import com.example.ui.dto.LoginRequest;
import com.example.ui.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthUIService {
    
    private final WebClient authWebClient;

    public Mono<Map> login(LoginRequest request) {
        return authWebClient.post()
                .uri("/api/auth/authenticate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> register(RegisterRequest request) {
        return authWebClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class);
    }
} 