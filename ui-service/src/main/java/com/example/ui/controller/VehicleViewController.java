package com.example.ui.controller;

import com.example.ui.service.VehicleUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vehicles")
public class VehicleViewController {

    private final VehicleUIService vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VENDOR')")
    public String listVehicles(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");

        /* 
        if (token == null) {
            return "redirect:/login";
        }
*/
        vehicleService.getAvailableVehicles(token)
                .subscribe(vehicles -> model.addAttribute("vehicles", vehicles));

        return "vehicle/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VENDOR')")
    public String vehicleDetails(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        /*
        if (token == null) {
            return "redirect:/login";
        }
        */

        vehicleService.getVehicleDetails(id, token)
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
} 