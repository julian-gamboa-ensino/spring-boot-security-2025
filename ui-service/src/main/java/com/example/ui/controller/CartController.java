package com.example.ui.controller;

import com.example.ui.service.CartUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartUIService cartService;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        
        cartService.getCart(token != null ? token : "")
                .subscribe(cart -> {
                    model.addAttribute("cart", cart);
                    // Verifica se o carrinho expirou
                    if (cart != null && cart.containsKey("expiresAt")) {
                        long expiresAt = ((Number) cart.get("expiresAt")).longValue();
                        if (System.currentTimeMillis() > expiresAt) {
                            model.addAttribute("error", "Seu carrinho expirou. Os itens foram removidos.");
                            cartService.cancelCart(token != null ? token : "").subscribe();
                        }
                    }
                });

        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long vehicleId, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        cartService.addToCart(vehicleId, token != null ? token : "")
                .subscribe(
                    success -> redirectAttributes.addFlashAttribute("success", "Item adicionado ao carrinho"),
                    error -> redirectAttributes.addFlashAttribute("error", "Erro ao adicionar ao carrinho")
                );

        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long vehicleId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        try {
            cartService.removeFromCart(vehicleId, token != null ? token : "")
                    .block();
            redirectAttributes.addFlashAttribute("success", "Item removido do carrinho");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao remover do carrinho");
        }

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isVendedor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));

        try {
            cartService.checkout(token != null ? token : "")
                    .block();
            redirectAttributes.addFlashAttribute("success", 
                isVendedor ? "Venda efetivada com sucesso" : "Compra efetivada com sucesso");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                isVendedor ? "Erro ao efetivar venda" : "Erro ao efetivar compra");
            return "redirect:/cart";
        }
    }

    @PostMapping("/cancel")
    public String cancelCart(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        try {
            cartService.cancelCart(token != null ? token : "")
                    .block();
            redirectAttributes.addFlashAttribute("success", "Carrinho cancelado com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar carrinho");
        }

        return "redirect:/vehicles";
    }
} 