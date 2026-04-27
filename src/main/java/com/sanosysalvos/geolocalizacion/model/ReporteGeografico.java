package com.sanosysalvos.geolocalizacion.model;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reportes_geograficos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGeografico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El ID de la mascota es obligatorio")
    @Column(name = "mascota_id")
    private Integer mascotaId; 

    @Column(columnDefinition = "geometry(Point,4326)") 
    private Point ubicacion;

    @Column(name = "radio_km")
    private Double radioKm;
    
    @Column(name = "es_activo")
    private Boolean esActivo = true;
}