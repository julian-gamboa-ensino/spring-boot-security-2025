package com.example.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String COMMERCE_SERVICE_URL = "http://commerce-service:8081";

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        try {
            Object cart = restTemplate.getForObject(
                COMMERCE_SERVICE_URL + "/api/cart",
                Object.class
            );
            model.addAttribute("cart", cart);
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar carrinho");
            return "error";
        }
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long vehicleId, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            restTemplate.postForObject(
                COMMERCE_SERVICE_URL + "/api/cart/add?vehicleId=" + vehicleId,
                null,
                Object.class
            );
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao adicionar ao carrinho");
            return "redirect:/vehicles";
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long vehicleId,
                               HttpSession session) {
        restTemplate.postForObject(
            COMMERCE_SERVICE_URL + "/api/cart/remove?vehicleId=" + vehicleId,
            null,
            Object.class
        );
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session) {
        try {
            restTemplate.postForObject(
                COMMERCE_SERVICE_URL + "/api/cart/checkout",
                null,
                Object.class
            );
            return "redirect:/cart";
        } catch (Exception e) {
            return "redirect:/cart";
        }
    }
} 