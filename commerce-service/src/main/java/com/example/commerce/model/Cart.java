package com.example.commerce.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade Carrinho
 * ==============
 * 
 * COMPOSIÇÃO:
 * ---------
 * - id: Long
 * - cliente: User
 * - veiculo: Vehicle
 * - dataAdicao: LocalDateTime
 * - status: Enum {ATIVO, EXPIRADO, FINALIZADO}
 * 
 * REGRAS TEMPORAIS:
 * --------------
 * - Tempo máximo: 1 minuto por item
 * - Cronômetro visível na tela
 * - Verificação automática de expiração
 * 
 * ESTADOS DO VEÍCULO:
 * ----------------
 * 1. Ao Adicionar:
 *    - Veículo fica indisponível
 *    - Timer inicia contagem
 *    - Status ATIVO
 * 
 * 2. Ao Cancelar/Expirar:
 *    - Veículo volta a disponível
 *    - Timer é cancelado
 *    - Status EXPIRADO
 * 
 * 3. Ao Finalizar:
 *    - Veículo permanece indisponível
 *    - Gera venda
 *    - Status FINALIZADO
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
    private String userId;

    @ManyToMany
    @JoinTable(
        name = "cart_vehicle",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    )
    private Set<Vehicle> vehicles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CartStatus status = CartStatus.ACTIVE;

    /**
     * Data/hora de criação do carrinho
     */
    private LocalDateTime createdAt;

    /**
     * Data/hora de expiração do carrinho
     */
    private LocalDateTime expirationTime;

    /**
     * Flag que indica se o carrinho foi finalizado (compra ou cancelamento)
     */
    private boolean finalizado = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Carrinho expira em 1 minuto
        expirationTime = createdAt.plusMinutes(1);
    }

    /**
     * Verifica se o carrinho está expirado
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * Adiciona um veículo ao carrinho
     */
    public void adicionarVeiculo(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    /**
     * Remove um veículo do carrinho
     */
    public void removerVeiculo(Vehicle vehicle) {
        vehicles.remove(vehicle);
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
        return vehicles.isEmpty();
    }

    private LocalDateTime expiresAt;

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
} 