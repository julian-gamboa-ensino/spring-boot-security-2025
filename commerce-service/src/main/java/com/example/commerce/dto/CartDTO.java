package com.example.commerce.dto;

import com.example.commerce.model.Cart;
import com.example.commerce.model.CartStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class CartDTO {
    private Long id;
    private String userId;
    private Set<VehicleDTO> vehicles;
    private CartStatus status;
    private LocalDateTime expirationTime;

    // Método para converter Cart em CartDTO
    public static CartDTO fromEntity(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setStatus(cart.getStatus());
        dto.setExpirationTime(cart.getExpirationTime());
        return dto;
    }

    public static List<Long> toIdList(List<Cart> cartList) {
        return cartList.stream()
        .map(Cart::getId) // Method reference
        .toList();
    }
}