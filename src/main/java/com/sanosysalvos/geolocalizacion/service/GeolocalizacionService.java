package com.sanosysalvos.geolocalizacion.service;

import java.util.List;
import java.util.Map;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeolocalizacionService {

    private final ReporteGeograficoRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Value("${locationiq.api.key}")
    private String apiKey;

    @Value("${locationiq.api.url}")
    private String apiUrl;

    public ReporteGeograficoResponseDTO registrarUbicacion(Integer mascotaId, String direccionStr) {
        String url = apiUrl + "?key=" + apiKey + "&q=" + direccionStr + "&format=json";
        LocationIqResponse[] response = restTemplate.getForObject(url, LocationIqResponse[].class);

        if (response != null && response.length > 0) {
            double lat = Double.parseDouble(response[0].getLat());
            double lon = Double.parseDouble(response[0].getLon());

            Point ubicacionPoint = geometryFactory.createPoint(new Coordinate(lon, lat));
            ubicacionPoint.setSRID(4326); 

            ReporteGeografico reporte = new ReporteGeografico();
            reporte.setMascotaId(mascotaId);
            reporte.setUbicacion(ubicacionPoint);
            reporte.setRadioKm(5.0); 
            
            return toDTO(repository.save(reporte));
        } else {
            throw new RuntimeException("No se pudo encontrar la dirección usando LocationIQ");
        }
    }

    public List<ReporteGeograficoResponseDTO> obtenerTodos() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReporteGeograficoResponseDTO obtenerPorId(Integer id) {
        ReporteGeografico reporte = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El reporte geográfico con ID " + id + " no existe"));
        return toDTO(reporte);
    }

    public ReporteGeograficoResponseDTO actualizarParcial(Integer id, Map<String, Object> campos) {
        ReporteGeografico reporte = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el reporte con ID " + id));

        if (campos.containsKey("radioKm")) {
            Object radioObj = campos.get("radioKm");
            Double nuevoRadio = radioObj instanceof Integer ? ((Integer) radioObj).doubleValue() : (Double) radioObj;
            reporte.setRadioKm(nuevoRadio);
        }

        if (campos.containsKey("esActivo")) {
            reporte.setEsActivo((Boolean) campos.get("esActivo"));
        }

        if (campos.containsKey("direccion")) {
            String nuevaDireccion = (String) campos.get("direccion");
            String url = apiUrl + "?key=" + apiKey + "&q=" + nuevaDireccion + "&format=json";
            LocationIqResponse[] response = restTemplate.getForObject(url, LocationIqResponse[].class);

            if (response != null && response.length > 0) {
                double lat = Double.parseDouble(response[0].getLat());
                double lon = Double.parseDouble(response[0].getLon());

                Point nuevaUbicacion = geometryFactory.createPoint(new Coordinate(lon, lat));
                nuevaUbicacion.setSRID(4326); 
                reporte.setUbicacion(nuevaUbicacion);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo geolocalizar la dirección");
            }
        }

        return toDTO(repository.save(reporte));
    }

    public void eliminarReporte(Integer id) {
        ReporteGeografico reporte = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID no encontrado"));
        repository.delete(reporte);
    }

    // Método helper para convertir Entidad a DTO
    private ReporteGeograficoResponseDTO toDTO(ReporteGeografico reporte) {
        return ReporteGeograficoResponseDTO.builder()
                .id(reporte.getId())
                .mascotaId(reporte.getMascotaId())
                .latitud(reporte.getUbicacion().getY()) // Y = Latitud
                .longitud(reporte.getUbicacion().getX()) // X = Longitud
                .radioKm(reporte.getRadioKm())
                .esActivo(reporte.getEsActivo())
                .build();
    }
}