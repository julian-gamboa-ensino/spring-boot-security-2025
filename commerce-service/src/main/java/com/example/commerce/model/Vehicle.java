package com.example.commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Entidade Veículo
 * ==============
 * 
 * ATRIBUTOS:
 * --------
 * - ano: Integer
 * - precoBase: BigDecimal
 * - cor: Enum {BRANCA, PRATA, PRETA}
 * - modelo: String
 * - disponivel: boolean
 * 
 * REGRAS DE PREÇO:
 * --------------
 * 1. Pessoa Jurídica:
 *    precoFinal = (precoBase + precoCor) * 0.8 (20% desconto)
 * 
 * 2. Pessoa Física PCD:
 *    precoFinal = (precoBase + precoCor) * 0.7 (30% desconto)
 * 
 * 3. Pessoa Física comum:
 *    precoFinal = precoBase + precoCor (sem desconto)
 * 
 * ESTADOS:
 * ------
 * - Disponível: pode ser adicionado ao carrinho
 * - Reservado: em carrinho (indisponível por 1 minuto)
 * - Vendido: baixa permanente no estoque
 */
@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ano do veículo
     */
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano deve ser maior que 1900")
    private Integer ano;

    /**
     * Preço do veículo
     */
    @NotNull(message = "Preço é obrigatório")
    @Min(value = 0, message = "Preço deve ser maior que zero")
    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    /**
     * Cor do veículo (BRANCA, PRATA ou PRETA)
     */
    @NotNull(message = "Cor é obrigatória")
    @Enumerated(EnumType.STRING)
    private VehicleColor color;

    /**
     * Modelo do veículo
     */
    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    /**
     * Flag que indica se o veículo está disponível para venda
     */
    private boolean disponivel = true;

    /**
     * Flag que indica se o veículo está vendido (indisponível permanentemente)
     */
    private boolean vendido = false;

    /**
     * ID do carrinho em que o veículo está, se estiver em algum
     */
    private Long carrinhoId;

    /**
     * Timestamp de quando o veículo foi adicionado ao carrinho atual
     */
    private Long carrinhoTimestamp;

    /**
     * Verifica se o veículo está disponível para venda
     */
    public boolean isDisponivel() {
        // Se não estiver marcado como disponível, retorna false
        if (!disponivel) return false;

        // Se não estiver em nenhum carrinho, está disponível
        if (carrinhoId == null) return true;

        // Se estiver em um carrinho, verifica se o tempo expirou
        long now = System.currentTimeMillis();
        return now > carrinhoTimestamp + 60000; // 1 minuto em milissegundos
    }

    /**
     * Adiciona o veículo a um carrinho
     */
    public void adicionarAoCarrinho(Long carrinhoId) {
        this.carrinhoId = carrinhoId;
        this.carrinhoTimestamp = System.currentTimeMillis();
    }

    /**
     * Remove o veículo do carrinho atual
     */
    public void removerDoCarrinho() {
        this.carrinhoId = null;
        this.carrinhoTimestamp = null;
    }

    /**
     * Marca o veículo como vendido (indisponível permanentemente)
     */
    public void marcarComoVendido() {
        this.disponivel = false;
        this.carrinhoId = null;
        this.carrinhoTimestamp = null;
    }

    @Version
    private Long version; // Para controle de concorrência otimista
} 