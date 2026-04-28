package com.sanosysalvos.geolocalizacion.service;

import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.sanosysalvos.geolocalizacion.dto.LocationIqResponse;
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

    public ReporteGeografico registrarUbicacion(Integer mascotaId, String direccionStr) {
        // 1. Llamar a LocationIQ
        String url = apiUrl + "?key=" + apiKey + "&q=" + direccionStr + "&format=json";
        LocationIqResponse[] response = restTemplate.getForObject(url, LocationIqResponse[].class);

        if (response != null && response.length > 0) {
            double lat = Double.parseDouble(response[0].getLat());
            double lon = Double.parseDouble(response[0].getLon());

            // 2. Crear el objeto Point de PostGIS
            Point ubicacionPoint = geometryFactory.createPoint(new Coordinate(lon, lat));
            ubicacionPoint.setSRID(4326); 

            // 3. Guardar en Base de Datos
            ReporteGeografico reporte = new ReporteGeografico();
            reporte.setMascotaId(mascotaId);
            reporte.setUbicacion(ubicacionPoint);
            reporte.setRadioKm(5.0); 
            
            return repository.save(reporte);
        } else {
            throw new RuntimeException("No se pudo encontrar la direcciÃ³n usando LocationIQ");
        }
    }

    public List<ReporteGeografico> obtenerTodos() {
        return repository.findAll();
    }

    // ... después de obtenerTodos()

    public ReporteGeografico obtenerPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El reporte geográfico con ID " + id + " no existe"));
    }

    public ReporteGeografico actualizarParcial(Integer id, Map<String, Object> campos) {
        ReporteGeografico reporte = obtenerPorId(id);

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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo geolocalizar la nueva dirección");
            }
        }

        return repository.save(reporte);
    }

    public void eliminarReporte(Integer id) {
        ReporteGeografico reporte = obtenerPorId(id);
        repository.delete(reporte);
    }
}