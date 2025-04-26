package com.example.commerce.repository;

import com.example.commerce.model.Cart;
import com.example.commerce.model.CartStatus;
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
    Optional<Cart> findByUserIdAndFinalizadoFalse(String userId);
    
    /**
     * Lista carrinhos expirados e não finalizados
     */
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :now AND c.finalizado = false")
    List<Cart> findExpiredCarts(LocalDateTime now);
    
    /**
     * Lista carrinhos por usuário
     */
    List<Cart> findByUserId(String userId);

    Cart findFirstByOrderByCreatedAtDesc();

    Optional<Cart> findByUserIdAndStatus(String userId, CartStatus status);
    boolean existsByVehiclesIdAndStatus(Long vehicleId, CartStatus status);

    List<Cart> findByStatusAndExpirationTimeBefore(CartStatus status, LocalDateTime time);
} 