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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

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
        // Obtém o token de autenticação da sessão
        String token = (String) session.getAttribute("token");
        
        // Busca os itens do carrinho e adiciona ao modelo
        cartService.getCart(token != null ? token : "")
                .subscribe(cart -> {
                    model.addAttribute("cart", cart);
                    // Verifica se o carrinho expirou
                    if (cart != null && cart.containsKey("expiresAt")) {
                        LocalDateTime expiresAt = LocalDateTime.parse(cart.get("expiresAt").toString());
                        LocalDateTime now = LocalDateTime.now();
                        
                        // Se o carrinho já expirou
                        if (now.isAfter(expiresAt)) {
                            model.addAttribute("error", "Seu carrinho expirou. Os itens foram removidos.");
                            cartService.cancelCart(token != null ? token : "").subscribe();
                        } else {
                            // Calcula o tempo restante em segundos
                            long timeLeft = ChronoUnit.SECONDS.between(now, expiresAt);
                            model.addAttribute("timeLeft", timeLeft);
                            
                            // Adiciona aviso se faltar menos de 30 segundos
                            if (timeLeft <= 30) {
                                model.addAttribute("warning", "Atenção: Seu carrinho expira em " + timeLeft + " segundos!");
                            }
                        }
                    }
                });

        // Adicione esta linha para expor o profile ativo
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "default";
        model.addAttribute("activeProfile", activeProfile);

        return "cart/cart";
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