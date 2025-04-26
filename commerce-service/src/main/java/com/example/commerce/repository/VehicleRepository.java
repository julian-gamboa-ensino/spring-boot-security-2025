package com.example.commerce.repository;

import com.example.commerce.model.Vehicle;
import com.example.commerce.model.VehicleColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositório de Veículos
 * ====================
 * 
 * OPERAÇÕES PRINCIPAIS:
 * -----------------
 * 1. Consultas:
 *    - Busca por disponibilidade
 *    - Filtro por características
 *    - Ordenação personalizada
 * 
 * 2. Atualizações:
 *    - Status de disponibilidade
 *    - Baixa em estoque
 *    - Reservas temporárias
 * 
 * DADOS INICIAIS:
 * ------------
 * - Veículos pré-cadastrados
 * - Script de inicialização
 * - Dados de exemplo
 * 
 * ÍNDICES:
 * -------
 * - Disponibilidade
 * - Modelo
 * - Preço
 * - Ano
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Lista todos os veículos disponíveis
     */
    List<Vehicle> findByDisponivelTrue();
    
    /**
     * Lista veículos por cor
     */
        
    List<Vehicle> findByColor(VehicleColor color);
    
    /**
     * Lista veículos por faixa de preço
     */
    List<Vehicle> findByPrecoLessThanEqualAndDisponivelTrue(BigDecimal precoMaximo);
    
    /**
     * Lista veículos que estão em um determinado carrinho
     */
    List<Vehicle> findByCarrinhoId(Long carrinhoId);
    
    /**
     * Busca veículos com timeout expirado em carrinhos
     */
    @Query("SELECT v FROM Vehicle v WHERE v.carrinhoId IS NOT NULL AND v.carrinhoTimestamp < :timestamp")
    List<Vehicle> findExpiredCartVehicles(Long timestamp);

    List<Vehicle> findByDisponivelTrueAndVendidoFalse();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
    Optional<Vehicle> findByIdWithLock(Long id);
} 