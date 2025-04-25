package com.example.commerce.controller;

import com.example.commerce.dto.VehicleDTO;
import com.example.commerce.model.Vehicle;
import com.example.commerce.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador responsável pelo gerenciamento de veículos.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Endpoints para gerenciamento de veículos")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/available")
    public ResponseEntity<List<VehicleDTO>> listAvailable() {
        return ResponseEntity.ok(vehicleService.listarDisponiveis());
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<VehicleDTO> reserveVehicle(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(vehicleService.reservarVeiculo(id, userId));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<VehicleDTO> releaseVehicle(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.liberarVeiculo(id));
    }

    @PostMapping("/{id}/sell")
    public ResponseEntity<VehicleDTO> markAsSold(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.marcarComoVendido(id));
    }

    @GetMapping
    @Operation(summary = "Lista veículos disponíveis", security = @SecurityRequirement(name = "jwt"))
    public ResponseEntity<List<VehicleDTO>> listarDisponiveis() {
        List<VehicleDTO> vehicles = vehicleService.listarDisponiveis();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca veículo por ID", security = @SecurityRequirement(name = "jwt"))
    public ResponseEntity<VehicleDTO> buscarPorId(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.buscarPorId(id);
        return ResponseEntity.ok(VehicleDTO.fromEntity(vehicle));
    }
} 