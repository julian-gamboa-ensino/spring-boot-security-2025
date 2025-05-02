package com.example.ui.controller;

import com.example.ui.service.CartUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Controlador responsável por gerenciar todas as operações relacionadas ao carrinho de compras.
 * Implementa as funcionalidades de visualização, adição, remoção, checkout e cancelamento do carrinho.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    // Injeção do serviço que gerencia as operações do carrinho
    private final CartUIService cartService;

    @Autowired
    private Environment environment;

    /**
     * Exibe a página do carrinho com os itens adicionados.
     * Verifica se o carrinho expirou e atualiza a interface conforme necessário.
     */
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        try {
            // Obtém o token de autenticação da sessão
            String token = (String) session.getAttribute("token");
            if (token == null) token = "";

            // Busca os itens do carrinho
            Map<String, Object> cart = cartService.getCart(token).block();
            
            // Adiciona o carrinho ao modelo, mesmo que seja null
            model.addAttribute("cart", cart);

            // Adiciona o profile ativo
            String[] activeProfiles = environment.getActiveProfiles();
            String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
            model.addAttribute("activeProfile", activeProfile);

            // Se o carrinho estiver vazio, adiciona uma mensagem
            if (cart == null || cart.isEmpty()) {
                model.addAttribute("info", "Carrinho vazio");
                return "cart/cart";
            }

            // Processa a data de expiração apenas se existir
            if (cart.containsKey("expiresAt")) {
                try {
                    long timestamp = Long.parseLong(cart.get("expiresAt").toString());
                    LocalDateTime expiresAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), 
                        ZoneId.systemDefault()
                    );
                    
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(expiresAt)) {
                        model.addAttribute("error", "Carrinho expirado");
                        cartService.cancelCart(token).block();
                    } else {
                        long timeLeft = ChronoUnit.SECONDS.between(now, expiresAt);
                        model.addAttribute("timeLeft", timeLeft);
                    }
                } catch (Exception e) {
                    model.addAttribute("warning", "Não foi possível verificar a expiração do carrinho");
                }
            }

            return "cart/cart";
            
        } catch (Exception e) {
            // Log do erro completo
            e.printStackTrace();
            model.addAttribute("error", "Erro ao processar o carrinho: " + e.getMessage());
            return "error/500"; // Página de erro genérica
        }
    }

    /**
     * Adiciona um veículo ao carrinho.
     * Recebe o ID do veículo e adiciona ao carrinho do usuário.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long vehicleId, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        // Tenta adicionar o veículo ao carrinho
        cartService.addToCart(vehicleId, token != null ? token : "")
                .subscribe(
                    // Em caso de sucesso, mostra mensagem positiva
                    success -> redirectAttributes.addFlashAttribute("success", "Item adicionado ao carrinho"),
                    // Em caso de erro, mostra mensagem de erro
                    error -> redirectAttributes.addFlashAttribute("error", "Erro ao adicionar ao carrinho")
                );

        return "redirect:/cart";
    }

    /**
     * Remove um veículo do carrinho.
     * Recebe o ID do veículo e remove do carrinho do usuário.
     */
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long vehicleId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        try {
            // Tenta remover o veículo do carrinho
            cartService.removeFromCart(vehicleId, token != null ? token : "")
                    .block(); // Aguarda a conclusão da operação
            redirectAttributes.addFlashAttribute("success", "Item removido do carrinho");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao remover do carrinho");
        }

        return "redirect:/cart";
    }

    /**
     * Processa o checkout do carrinho.
     * Verifica se o usuário é vendedor ou cliente e processa a venda/compra.
     */
    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");
        // Verifica o perfil do usuário (VENDEDOR ou CLIENTE)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isVendedor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"));

        try {
            // Processa o checkout
            cartService.checkout(token != null ? token : "")
                    .block();
            // Mostra mensagem de sucesso específica para o perfil
            redirectAttributes.addFlashAttribute("success", 
                isVendedor ? "Venda efetivada com sucesso" : "Compra efetivada com sucesso");
            return "redirect:/vehicles";
        } catch (Exception e) {
            // Mostra mensagem de erro específica para o perfil
            redirectAttributes.addFlashAttribute("error", 
                isVendedor ? "Erro ao efetivar venda" : "Erro ao efetivar compra");
            return "redirect:/cart";
        }
    }

    /**
     * Cancela o carrinho atual.
     * Remove todos os itens e retorna à lista de veículos.
     */
    @PostMapping("/cancel")
    public String cancelCart(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("token");

        try {
            // Cancela o carrinho
            cartService.cancelCart(token != null ? token : "")
                    .block();
            redirectAttributes.addFlashAttribute("success", "Carrinho cancelado com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar carrinho");
        }

        return "redirect:/vehicles";
    }
} 