package com.example.ui.controller;

import com.example.ui.service.CartUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {

    private final CartUIService cartService;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }

        cartService.getCart(token)
                .subscribe(cart -> model.addAttribute("cart", cart));

        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long vehicleId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }

        cartService.addToCart(vehicleId, token)
                .subscribe(
                    success -> {
                        redirectAttributes.addFlashAttribute("message", "Veículo adicionado ao carrinho");
                        redirectAttributes.addFlashAttribute("alertClass", "alert-success");
                    },
                    error -> {
                        redirectAttributes.addFlashAttribute("message", "Erro ao adicionar veículo");
                        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    }
                );

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }

        cartService.checkout(token)
                .subscribe(
                    success -> {
                        redirectAttributes.addFlashAttribute("message", "Compra realizada com sucesso");
                        redirectAttributes.addFlashAttribute("alertClass", "alert-success");
                    },
                    error -> {
                        redirectAttributes.addFlashAttribute("message", "Erro ao finalizar compra");
                        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
                    }
                );

        return "redirect:/vehicles";
    }
} 