package com.example.ui.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleUIService {

    private final WebClient commerceWebClient;

    public Mono<List> getAvailableVehicles(String token) {
        return commerceWebClient.get()
                .uri("/api/vehicles/available")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class);
    }

    public Mono<Map> getVehicleDetails(Long id, String token) {
        return commerceWebClient.get()
                .uri("/api/vehicles/" + id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class);
    }
} 