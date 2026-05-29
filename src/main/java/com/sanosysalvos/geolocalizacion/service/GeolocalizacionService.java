package com.sanosysalvos.geolocalizacion.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.sanosysalvos.geolocalizacion.dto.LocationIqResponse;
import com.sanosysalvos.geolocalizacion.dto.ReporteGeograficoResponseDTO;
import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;
import com.sanosysalvos.geolocalizacion.repository.ReporteGeograficoRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GeolocalizacionService {

    private final ReporteGeograficoRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Value("${locationiq.api.key}")
    private String apiKey;

    @Value("${locationiq.api.url}")
    private String apiUrl;

    @CircuitBreaker(name = "locationIqCB", fallbackMethod = "fallbackRegistrarUbicacion")
    public ReporteGeograficoResponseDTO registrarUbicacion(Integer mascotaId, String direccionStr) {
        Optional<ReporteGeografico> reporteExistente = repository.findByMascotaId(mascotaId);
        if (reporteExistente.isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Error: La mascota con ID " + mascotaId + " ya cuenta con una ubicación geográfica registrada."
            );
        }

        Point ubicacionPoint = geocodificarDireccion(direccionStr);
        ReporteGeografico reporte = new ReporteGeografico();
        reporte.setMascotaId(mascotaId);
        reporte.setUbicacion(ubicacionPoint);
        reporte.setRadioKm(5.0);

        return toDTO(repository.save(reporte));
    }

    public ReporteGeograficoResponseDTO fallbackRegistrarUbicacion(Integer mascotaId, String direccionStr, Throwable t) {
        if (t instanceof ResponseStatusException) {
            throw (ResponseStatusException) t;
        }
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Servicio externo de mapas no disponible temporalmente. (Circuit Breaker Activo)");
    }

    public List<ReporteGeograficoResponseDTO> obtenerTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReporteGeograficoResponseDTO obtenerPorId(Integer id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El reporte geográfico con ID " + id + " no existe"));
    }

    @CircuitBreaker(name = "locationIqCB", fallbackMethod = "fallbackActualizarParcial")
    public ReporteGeograficoResponseDTO actualizarParcial(Integer id, Map<String, Object> campos) {
        ReporteGeografico reporte = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el reporte con ID " + id));

        if (campos.containsKey("radioKm")) {
            Object radioObj = campos.get("radioKm");
            if (radioObj instanceof Number) {
                reporte.setRadioKm(((Number) radioObj).doubleValue());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El valor de radioKm debe ser un número");
            }
        }

        if (campos.containsKey("esActivo")) {
            reporte.setEsActivo((Boolean) campos.get("esActivo"));
        }

        if (campos.containsKey("direccion")) {
            String nuevaDireccion = (String) campos.get("direccion");
            reporte.setUbicacion(geocodificarDireccion(nuevaDireccion));
        }

        return toDTO(repository.save(reporte));
    }

    public ReporteGeograficoResponseDTO fallbackActualizarParcial(Integer id, Map<String, Object> campos, Throwable t) {
        if (t instanceof ResponseStatusException) {
            throw (ResponseStatusException) t;
        }
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "No se puede actualizar la dirección porque el servicio externo falló. Intente más tarde.");
    }

    public void eliminarReporte(Integer id) {
        ReporteGeografico reporte = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID no encontrado"));
        repository.delete(reporte);
    }

    private Point geocodificarDireccion(String direccionStr) {
        String urlTemplate = apiUrl + "?key={key}&q={q}&format=json";
        LocationIqResponse[] response = restTemplate.getForObject(
            urlTemplate, LocationIqResponse[].class, apiKey, direccionStr
        );

        if (response != null && response.length > 0) {
            double lat = Double.parseDouble(response[0].getLat());
            double lon = Double.parseDouble(response[0].getLon());

            Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
            point.setSRID(4326);
            return point;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se pudo encontrar la dirección en LocationIQ");
        }
    }

    private ReporteGeograficoResponseDTO toDTO(ReporteGeografico reporte) {
        return ReporteGeograficoResponseDTO.builder()
                .id(reporte.getId())
                .mascotaId(reporte.getMascotaId())
                .latitud(reporte.getUbicacion().getY())
                .longitud(reporte.getUbicacion().getX())
                .radioKm(reporte.getRadioKm())
                .esActivo(reporte.getEsActivo())
                .build();
    }
}