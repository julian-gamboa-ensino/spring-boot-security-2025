package com.example.commerce.repository;

import com.example.commerce.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para a entidade Sale.
 * Fornece operações de banco de dados para vendas.
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    /**
     * Lista vendas por usuário
     */
    List<Sale> findByUserId(String userId);
    
    /**
     * Lista vendas por tipo
     */
    List<Sale> findByTipo(Sale.SaleType tipo);
    
    /**
     * Lista vendas por período
     */
    List<Sale> findByDataVendaBetween(LocalDateTime inicio, LocalDateTime fim);
} 