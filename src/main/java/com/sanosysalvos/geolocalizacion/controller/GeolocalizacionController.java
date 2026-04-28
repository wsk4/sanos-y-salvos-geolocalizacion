package com.sanosysalvos.geolocalizacion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // ... después de listarUbicaciones()

    @GetMapping("/{id}")
    public ResponseEntity<ReporteGeografico> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReporteGeografico> actualizarReporteParcial(
            @PathVariable Integer id, 
            @RequestBody Map<String, Object> campos) {
        return ResponseEntity.ok(service.actualizarParcial(id, campos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Integer id) {
        service.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }
}