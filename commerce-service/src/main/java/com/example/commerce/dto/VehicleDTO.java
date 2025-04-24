package com.example.commerce.dto;

import com.example.commerce.model.Vehicle;
import lombok.Data;

@Data
public class VehicleDTO {
    private Long id;
    private String modelo;
    private String cor;
    private int ano;
    private double preco;

    // Método para converter Vehicle em VehicleDTO
    public static VehicleDTO fromEntity(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setModelo(vehicle.getModelo());
        dto.setAno(vehicle.getAno());
        return dto;
    }
}
