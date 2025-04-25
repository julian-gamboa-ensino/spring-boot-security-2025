package com.example.commerce.mapper;

import com.example.commerce.dto.VehicleDTO;
import com.example.commerce.model.Vehicle;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleMapper {
    
    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null) return null;
        
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
    
    public List<VehicleDTO> toDTOList(List<Vehicle> vehicles) {
        if (vehicles == null) return null;
        return vehicles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
} 