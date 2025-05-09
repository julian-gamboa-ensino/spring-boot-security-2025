package com.example.commerce.service;

import com.example.commerce.dto.VehicleDTO;
import com.example.commerce.exception.BusinessException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.mapper.VehicleMapper;
import com.example.commerce.model.Vehicle;
import com.example.commerce.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Serviço de Gestão de Veículos
 * ==========================
 * 
 * DADOS PRÉ-CADASTRADOS:
 * -------------------
 * - Veículos carregados na inicialização
 * - Script SQL com dados iniciais
 * - Atualização via admin
 * 
 * ATRIBUTOS DO VEÍCULO:
 * ------------------
 * 1. Básicos:
 *    - ano: Integer
 *    - precoBase: BigDecimal
 *    - modelo: String
 * 
 * 2. Cor (impacta preço):
 *    BRANCA: preço base
 *    PRATA: +R$ 2.000,00
 *    PRETA: +R$ 1.000,00
 * 
 * DISPONIBILIDADE:
 * -------------
 * - Status atualizado em tempo real
 * - Bloqueio automático ao adicionar no carrinho
 * - Liberação automática após expiração/cancelamento
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final CartService cartService;

    /**
     * Lista todos os veículos disponíveis
     */
    public List<VehicleDTO> listarDisponiveis() {
        return vehicleMapper.toDTOList(
            vehicleRepository.findByDisponivelTrueAndVendidoFalse()
        );
    }

    /**
     * Cria um novo veículo
     */
    @Transactional
    public VehicleDTO createVehicle(VehicleDTO vehicleDto) {
        Vehicle vehicle = vehicleMapper.toEntity(vehicleDto);
        vehicle.setDisponivel(true);
        vehicle.setVendido(false);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toDTO(savedVehicle);
    }

    /**
     * Deleta um veículo
     */
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (!vehicle.isDisponivel() || vehicle.isVendido()) {
            throw new BusinessException("Não é possível deletar um veículo que não está disponível");
        }

        vehicleRepository.delete(vehicle);
    }

    /**
     * Busca veículo por ID
     */
    public Vehicle buscarPorId(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
    }

    /**
     * Adiciona veículo ao carrinho
     */
    @Transactional
    public VehicleDTO reservarVeiculo(Long vehicleId, String userId) {
        Vehicle vehicle = buscarPorId(vehicleId);
        
        if (!vehicle.isDisponivel()) {
            throw new BusinessException("Veículo não está disponível");
        }

        if (cartService.isVehicleInActiveCart(vehicleId)) {
            throw new BusinessException("Veículo já está em um carrinho ativo");
        }

        vehicle.setDisponivel(false);
        vehicleRepository.save(vehicle);
        
        cartService.addVehicleToCart(vehicleId, userId);
        return vehicleMapper.toDTO(vehicle);
    }

    /**
     * Remove veículo do carrinho
     */
    @Transactional
    public VehicleDTO liberarVeiculo(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (vehicle.isVendido()) {
            throw new BusinessException("Veículo já foi vendido");
        }

        vehicle.setDisponivel(true);
        vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(vehicle);
    }

    /**
     * Marca veículo como vendido
     */
    @Transactional
    public VehicleDTO marcarComoVendido(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (vehicle.isVendido()) {
            throw new BusinessException("Veículo já foi vendido");
        }

        vehicle.setVendido(true);
        vehicle.setDisponivel(false);
        vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(vehicle);
    }
} 