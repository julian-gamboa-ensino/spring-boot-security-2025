package com.example.ui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUIService {

    private final WebClient authWebClient;

    public Mono<List> getAllUsers(String token) {
        return authWebClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class);
    }
} 