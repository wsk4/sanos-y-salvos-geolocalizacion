package com.sanosysalvos.geolocalizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGeograficoResponseDTO {
    private Integer id;
    private Integer mascotaId;
    private Double latitud;
    private Double longitud;
    private Double radioKm;
    private Boolean esActivo;
}