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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleUIService {

    private final WebClient commerceWebClient;
    private final Environment environment;

    @Value("classpath:mock/vehicles.json")
    private Resource vehiclesMockResource;

    public Mono<List> getAvailableVehicles(String token) {
        if (isDevProfile()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> mockVehicles = mapper.readValue(
                    vehiclesMockResource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                return Mono.just(mockVehicles);
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Erro ao carregar mock de veículos", e));
            }
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