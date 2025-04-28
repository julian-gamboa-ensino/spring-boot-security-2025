package com.example.ui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartUIService {

    private final WebClient commerceWebClient;
    private final Environment environment;

    public Mono<Map> addToCart(Long vehicleId, String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "Item adicionado ao carrinho");
            return Mono.just(mockResponse);
        }

        return commerceWebClient.post()
                .uri("/api/cart/add/" + vehicleId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> getCart(String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockCart = new HashMap<>();
            Map<String, Object> mockItem = new HashMap<>();
            Map<String, Object> mockVehicle = new HashMap<>();
            
            mockVehicle.put("id", 1L);
            mockVehicle.put("modelo", "Toyota Corolla");
            mockVehicle.put("ano", 2023);
            mockVehicle.put("preco", 125000.00);
            mockVehicle.put("cor", "Prata");
            
            mockItem.put("vehicle", mockVehicle);
            mockCart.put("items", java.util.Collections.singletonList(mockItem));
            mockCart.put("total", 125000.00);
            mockCart.put("expiresAt", System.currentTimeMillis() + 60000); // 1 minuto
            
            return Mono.just(mockCart);
        }

        return commerceWebClient.get()
                .uri("/api/cart")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> removeFromCart(Long vehicleId, String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "Item removido do carrinho");
            return Mono.just(mockResponse);
        }

        return commerceWebClient.post()
                .uri("/api/cart/remove/" + vehicleId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> checkout(String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "Compra realizada com sucesso");
            return Mono.just(mockResponse);
        }

        return commerceWebClient.post()
                .uri("/api/cart/checkout")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> cancelCart(String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "Carrinho cancelado com sucesso");
            return Mono.just(mockResponse);
        }

        return commerceWebClient.post()
                .uri("/api/cart/cancel")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equalsIgnoreCase("dev")) {
                return true;
            }
        }
        return false;
    }
} 