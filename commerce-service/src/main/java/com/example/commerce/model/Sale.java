package com.example.commerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma venda no sistema.
 * Registra tanto vendas online (cliente) quanto físicas (vendedor).
 */
@Entity
@Table(name = "sales")
@Data
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID do usuário que realizou a compra
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Lista de IDs dos veículos vendidos
     */
    @ElementCollection
    @CollectionTable(name = "sale_vehicles", joinColumns = @JoinColumn(name = "sale_id"))
    @Column(name = "vehicle_id")
    private List<Long> vehicleIds = new ArrayList<>();

    /**
     * Valor total da venda
     */
    @Column(nullable = false)
    private BigDecimal valorTotal;

    /**
     * Data/hora da venda
     */
    private LocalDateTime dataVenda;

    /**
     * Tipo da venda (ONLINE ou FISICA)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleType tipo;

    @PrePersist
    protected void onCreate() {
        dataVenda = LocalDateTime.now();
    }

    /**
     * Tipo de venda
     */
    public enum SaleType {
        ONLINE,  // Venda realizada pelo cliente através do site
        FISICA   // Venda realizada por um vendedor
    }
} 