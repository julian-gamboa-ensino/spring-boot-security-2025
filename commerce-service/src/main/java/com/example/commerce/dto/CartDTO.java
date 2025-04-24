package com.example.commerce.dto;

import com.example.commerce.model.Cart;
import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private boolean finalizado;

    // Método para converter Cart em CartDTO
    public static CartDTO fromEntity(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setFinalizado(cart.isFinalizado());
        return dto;
    }

    public static List<Long> toIdList(List<Cart> cartList) {
        return cartList.stream()
        .map(Cart::getId) // Method reference
        .toList();
    }
}