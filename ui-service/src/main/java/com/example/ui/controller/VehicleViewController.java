package com.example.ui.controller;

import com.example.ui.service.VehicleUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleViewController {

    private final VehicleUIService vehicleService;
    private final Environment environment; // <-- Adicionado para acessar o profile ativo

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VENDOR')")
    public String listVehicles(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (isProdProfile() && token == null) {
            return "redirect:/login";
        }

        vehicleService.getAvailableVehicles(token != null ? token : "")
                .onErrorResume(ex -> isDevProfile() ? Mono.empty() : Mono.error(ex)) // <-- ignora erro só no DEV
                .subscribe(vehicles -> model.addAttribute("vehicles", vehicles));

        return "vehicle/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VENDOR')")
    public String vehicleDetails(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (isProdProfile() && token == null) {
            return "redirect:/login";
        }

        vehicleService.getVehicleDetails(id, token != null ? token : "")
                .onErrorResume(ex -> isDevProfile() ? Mono.empty() : Mono.error(ex))
                .subscribe(vehicle -> model.addAttribute("vehicle", vehicle));

        return "vehicle/details";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('VENDOR')")
    public String manageVehicles() {
        return "vehicle/manage";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel() {
        return "vehicle/admin";
    }

    private boolean isDevProfile() {
        return isProfileActive("dev");
    }

    private boolean isProdProfile() {
        return isProfileActive("prod");
    }

    private boolean isProfileActive(String profile) {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (activeProfile.equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
