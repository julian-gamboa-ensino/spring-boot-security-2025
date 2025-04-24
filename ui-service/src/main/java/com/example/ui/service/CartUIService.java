package com.example.ui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartUIService {

    private final WebClient commerceWebClient;

    public Mono<Map> addToCart(Long vehicleId, String token) {
        return commerceWebClient.post()
                .uri("/api/cart/add/" + vehicleId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> getCart(String token) {
        return commerceWebClient.get()
                .uri("/api/cart")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> checkout(String token) {
        return commerceWebClient.post()
                .uri("/api/cart/checkout")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }
} 