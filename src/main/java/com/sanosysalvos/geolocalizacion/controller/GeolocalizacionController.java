package com.sanosysalvos.geolocalizacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanosysalvos.geolocalizacion.dto.UbicacionRequestDTO;
import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;
import com.sanosysalvos.geolocalizacion.service.GeolocalizacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/geolocalizacion")
@RequiredArgsConstructor
public class GeolocalizacionController {

    private final GeolocalizacionService service;

    @PostMapping
    public ResponseEntity<ReporteGeografico> registrarUbicacion(@Valid @RequestBody UbicacionRequestDTO request) {
        //El Controller extrae los datos del DTO y llama al nuevo método del Service
        ReporteGeografico nuevoReporte = service.registrarUbicacion(
                request.getMascotaId(), 
                request.getDireccion()
        );
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED); 
    }

    @GetMapping
    public ResponseEntity<List<ReporteGeografico>> listarUbicaciones() {
        return ResponseEntity.ok(service.obtenerTodos());
    }
}