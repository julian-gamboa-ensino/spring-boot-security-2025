package com.example.commerce.dto;

import com.example.commerce.model.Vehicle;
import com.example.commerce.model.VehicleColor;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VehicleDTO {
    private Long id;
    private String modelo;
    private Integer ano;
    private VehicleColor color;
    private BigDecimal preco;
    private boolean disponivel;
    private boolean vendido;

    // Método para converter Vehicle em VehicleDTO
    public static VehicleDTO fromEntity(Vehicle vehicle) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setModelo(vehicle.getModelo());
        dto.setAno(vehicle.getAno());
        dto.setColor(vehicle.getColor());
        dto.setPreco(vehicle.getPreco());
        dto.setDisponivel(vehicle.isDisponivel());
        dto.setVendido(vehicle.isVendido());
        return dto;
    }
}
