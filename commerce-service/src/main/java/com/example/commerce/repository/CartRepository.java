package com.example.commerce.repository;

import com.example.commerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Cart.
 * Fornece operações de banco de dados para carrinhos.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Busca o carrinho ativo de um usuário
     */
    Optional<Cart> findByUserIdAndFinalizadoFalse(Long userId);
    
    /**
     * Lista carrinhos expirados e não finalizados
     */
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :now AND c.finalizado = false")
    List<Cart> findExpiredCarts(LocalDateTime now);
    
    /**
     * Lista carrinhos por usuário
     */
    List<Cart> findByUserId(Long userId);
} 