package com.example.ui.controller;

import com.example.ui.service.VehicleUIService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class VehicleViewController {

    private final VehicleUIService vehicleService;

    @GetMapping("/vehicles")
    public String listVehicles(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }

        vehicleService.getAvailableVehicles(token)
                .subscribe(vehicles -> model.addAttribute("vehicles", vehicles));

        return "vehicle/list";
    }

    @GetMapping("/vehicles/{id}")
    public String vehicleDetails(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "redirect:/login";
        }

        vehicleService.getVehicleDetails(id, token)
                .subscribe(vehicle -> model.addAttribute("vehicle", vehicle));

        return "vehicle/details";
    }
} 