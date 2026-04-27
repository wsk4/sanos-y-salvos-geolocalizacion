package com.sanosysalvos.geolocalizacion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;
import com.sanosysalvos.geolocalizacion.repository.ReporteGeograficoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class GeolocalizacionService {

    private final ReporteGeograficoRepository repository;

    public ReporteGeografico registrarUbicacion(ReporteGeografico reporte) {
        return repository.save(reporte);
    }

    public List<ReporteGeografico> obtenerTodos() {
        return repository.findAll();
    }
}