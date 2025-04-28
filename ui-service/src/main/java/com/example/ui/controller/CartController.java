package com.example.ui.controller;

import com.example.ui.service.CartUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartUIService cartService;
    private final Environment environment;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (isDevProfile()) {
            // Mock data for development
            Map<String, Object> mockCart = new HashMap<>();
            Map<String, Object> mockItem = new HashMap<>();
            Map<String, Object> mockVehicle = new HashMap<>();
            
            mockVehicle.put("id", 1L);
            mockVehicle.put("modelo", "Toyota Corolla");
            mockVehicle.put("preco", 125000.00);
            
            mockItem.put("vehicle", mockVehicle);
            mockCart.put("items", java.util.Collections.singletonList(mockItem));
            mockCart.put("total", 125000.00);
            mockCart.put("expiresAt", System.currentTimeMillis() + 300000); // 5 minutos
            
            model.addAttribute("cart", mockCart);
            return "cart/cart";
        }

        cartService.getCart(token != null ? token : "")
                .subscribe(cart -> model.addAttribute("cart", cart));

        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long vehicleId, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        if (isDevProfile()) {
            return "redirect:/cart";
        }

        cartService.addToCart(vehicleId, token != null ? token : "")
                .subscribe(
                    success -> {},
                    error -> redirectAttributes.addFlashAttribute("error", "Erro ao adicionar ao carrinho")
                );

        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long vehicleId,
                               HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (isDevProfile()) {
            return "redirect:/cart";
        }

        cartService.removeFromCart(vehicleId, token != null ? token : "")
                .subscribe();

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (isDevProfile()) {
            return "redirect:/vehicles";
        }

        cartService.checkout(token != null ? token : "")
                .subscribe();

        return "redirect:/vehicles";
    }

    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equalsIgnoreCase("dev")) {
                return true;
            }
        }
        return false;
    }
} 