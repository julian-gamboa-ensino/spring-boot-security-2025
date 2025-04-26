package com.example.commerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Venda
 * ============
 * 
 * ATRIBUTOS:
 * --------
 * - id: Long
 * - tipo: Enum {ONLINE, FISICA}
 * - cliente: User
 * - vendedor: User (opcional)
 * - veiculo: Vehicle
 * - valor: BigDecimal
 * - data: LocalDateTime
 * 
 * TIPOS DE VENDA:
 * ------------
 * 1. Online (CLIENTE):
 *    - Sem vendedor
 *    - Preço calculado automaticamente
 *    - Requer carrinho válido
 * 
 * 2. Física (VENDEDOR):
 *    - Vendedor obrigatório
 *    - Preço pode ser negociado
 *    - Registro direto
 * 
 * CÁLCULO DE PREÇO:
 * --------------
 * 1. Preço Base + Adicional Cor:
 *    - BRANCA: +0
 *    - PRATA: +2000
 *    - PRETA: +1000
 * 
 * 2. Descontos por Tipo:
 *    - Jurídica: 20% no total
 *    - PCD: 30% no total
 *    - Comum: sem desconto
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
    private String userId;

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