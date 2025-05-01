package com.example.ui.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleUIService {

    private final WebClient commerceWebClient;
    private final Environment environment;

    public Mono<List> getAvailableVehicles(String token) {
        if (isDevProfile()) {
            // Mock data for development
            List<Map<String, Object>> mockVehicles = new ArrayList<>();
            
            Map<String, Object> vehicle1 = new HashMap<>();
            vehicle1.put("id", 1L);
            vehicle1.put("modelo", "Toyota Corolla");
            vehicle1.put("ano", 2023);
            vehicle1.put("preco", 125000.00);
            vehicle1.put("cor", "Prata");
            mockVehicles.add(vehicle1);

            Map<String, Object> vehicle2 = new HashMap<>();
            vehicle2.put("id", 2L);
            vehicle2.put("modelo", "Honda Civic");
            vehicle2.put("ano", 2024);
            vehicle2.put("preco", 135000.00);
            vehicle2.put("cor", "Preto");
            mockVehicles.add(vehicle2);

            Map<String, Object> vehicle3 = new HashMap<>();
            vehicle3.put("id", 3L);
            vehicle3.put("modelo", "Volkswagen Golf");
            vehicle3.put("ano", 2023);
            vehicle3.put("preco", 145000.00);
            vehicle3.put("cor", "Branco");
            mockVehicles.add(vehicle3);

            Map<String, Object> vehicle4 = new HashMap<>();
            vehicle4.put("id", 4L);
            vehicle4.put("modelo", "Chevrolet Onix");
            vehicle4.put("ano", 2024);
            vehicle4.put("preco", 85000.00);
            vehicle4.put("cor", "Vermelho");
            mockVehicles.add(vehicle4);

            Map<String, Object> vehicle5 = new HashMap<>();
            vehicle5.put("id", 5L);
            vehicle5.put("modelo", "Fiat Argo");
            vehicle5.put("ano", 2023);
            vehicle5.put("preco", 75000.00);
            vehicle5.put("cor", "Azul");
            mockVehicles.add(vehicle5);

            Map<String, Object> vehicle6 = new HashMap<>();
            vehicle6.put("id", 6L);
            vehicle6.put("modelo", "Hyundai HB20");
            vehicle6.put("ano", 2024);
            vehicle6.put("preco", 82000.00);
            vehicle6.put("cor", "Cinza");
            mockVehicles.add(vehicle6);

            Map<String, Object> vehicle7 = new HashMap<>();
            vehicle7.put("id", 7L);
            vehicle7.put("modelo", "Renault Kwid");
            vehicle7.put("ano", 2023);
            vehicle7.put("preco", 65000.00);
            vehicle7.put("cor", "Verde");
            mockVehicles.add(vehicle7);

            Map<String, Object> vehicle8 = new HashMap<>();
            vehicle8.put("id", 8L);
            vehicle8.put("modelo", "Jeep Renegade");
            vehicle8.put("ano", 2024);
            vehicle8.put("preco", 155000.00);
            vehicle8.put("cor", "Laranja");
            mockVehicles.add(vehicle8);

            Map<String, Object> vehicle9 = new HashMap<>();
            vehicle9.put("id", 9L);
            vehicle9.put("modelo", "Nissan Kicks");
            vehicle9.put("ano", 2023);
            vehicle9.put("preco", 125000.00);
            vehicle9.put("cor", "Marrom");
            mockVehicles.add(vehicle9);

            Map<String, Object> vehicle10 = new HashMap<>();
            vehicle10.put("id", 10L);
            vehicle10.put("modelo", "Ford Ranger");
            vehicle10.put("ano", 2024);
            vehicle10.put("preco", 245000.00);
            vehicle10.put("cor", "Prata");
            mockVehicles.add(vehicle10);

            Map<String, Object> vehicle11 = new HashMap<>();
            vehicle11.put("id", 11L);
            vehicle11.put("modelo", "Toyota Hilux");
            vehicle11.put("ano", 2023);
            vehicle11.put("preco", 255000.00);
            vehicle11.put("cor", "Preto");
            mockVehicles.add(vehicle11);

            return Mono.just(mockVehicles);
        }

        return commerceWebClient.get()
                .uri("/api/vehicles/available")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(List.class);
    }

    public Mono<Map> getVehicleDetails(Long id, String token) {
        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockVehicle = new HashMap<>();
            mockVehicle.put("id", id);
            mockVehicle.put("modelo", "Veículo Mock " + id);
            mockVehicle.put("ano", 2023);
            mockVehicle.put("preco", 100000.00);
            mockVehicle.put("cor", "Preto");
            mockVehicle.put("descricao", "Descrição detalhada do veículo mock");
            mockVehicle.put("marca", "Marca Mock");
            mockVehicle.put("quilometragem", 0);
            mockVehicle.put("cambio", "Automático");
            mockVehicle.put("combustivel", "Flex");
            
            return Mono.just(mockVehicle);
        }

        return commerceWebClient.get()
                .uri("/api/vehicles/" + id)
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