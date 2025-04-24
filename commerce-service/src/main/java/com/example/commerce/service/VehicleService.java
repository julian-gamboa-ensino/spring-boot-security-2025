package com.example.commerce.service;

import com.example.commerce.model.Vehicle;
import com.example.commerce.model.VehicleColor;
import com.example.commerce.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio relacionada a veículos.
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /**
     * Lista todos os veículos disponíveis
     */
    public List<Vehicle> listarDisponiveis() {
        return vehicleRepository.findByDisponivelTrue();
    }

    /**
     * Busca veículo por ID
     */
    public Vehicle buscarPorId(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
    }

    /**
     * Adiciona veículo ao carrinho
     */
    @Transactional
    public void adicionarAoCarrinho(Long vehicleId, Long carrinhoId) {
        Vehicle vehicle = buscarPorId(vehicleId);
        if (!vehicle.isDisponivel()) {
            throw new RuntimeException("Veículo não está disponível");
        }
        vehicle.adicionarAoCarrinho(carrinhoId);
        vehicleRepository.save(vehicle);
    }

    /**
     * Remove veículo do carrinho
     */
    @Transactional
    public void removerDoCarrinho(Long vehicleId) {
        Vehicle vehicle = buscarPorId(vehicleId);
        vehicle.removerDoCarrinho();
        vehicleRepository.save(vehicle);
    }

    /**
     * Marca veículo como vendido
     */
    @Transactional
    public void marcarComoVendido(Long vehicleId) {
        Vehicle vehicle = buscarPorId(vehicleId);
        vehicle.marcarComoVendido();
        vehicleRepository.save(vehicle);
    }
} 