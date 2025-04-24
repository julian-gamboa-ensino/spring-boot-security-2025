package com.example.commerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um carrinho de compras no sistema.
 * Implementa a lógica de timeout e controle de itens.
 */
@Entity
@Table(name = "carts")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID do usuário dono do carrinho
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Lista de IDs dos veículos no carrinho
     */
    @ElementCollection
    @CollectionTable(name = "cart_vehicles", joinColumns = @JoinColumn(name = "cart_id"))
    @Column(name = "vehicle_id")
    private List<Long> vehicleIds = new ArrayList<>();

    /**
     * Data/hora de criação do carrinho
     */
    private LocalDateTime createdAt;

    /**
     * Data/hora de expiração do carrinho
     */
    private LocalDateTime expiresAt;

    /**
     * Flag que indica se o carrinho foi finalizado (compra ou cancelamento)
     */
    private boolean finalizado = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Carrinho expira em 1 minuto
        expiresAt = createdAt.plusMinutes(1);
    }

    /**
     * Verifica se o carrinho está expirado
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Adiciona um veículo ao carrinho
     */
    public void adicionarVeiculo(Long vehicleId) {
        if (!vehicleIds.contains(vehicleId)) {
            vehicleIds.add(vehicleId);
        }
    }

    /**
     * Remove um veículo do carrinho
     */
    public void removerVeiculo(Long vehicleId) {
        vehicleIds.remove(vehicleId);
    }

    /**
     * Finaliza o carrinho (seja por compra ou cancelamento)
     */
    public void finalizar() {
        this.finalizado = true;
    }

    /**
     * Verifica se o carrinho está vazio
     */
    public boolean isVazio() {
        return vehicleIds.isEmpty();
    }
} 