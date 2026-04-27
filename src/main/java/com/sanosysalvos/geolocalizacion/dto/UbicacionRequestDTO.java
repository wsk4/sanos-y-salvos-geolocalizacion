package com.sanosysalvos.geolocalizacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UbicacionRequestDTO {

    @NotNull(message = "El ID de la mascota es obligatorio")
    private Integer mascotaId;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
}