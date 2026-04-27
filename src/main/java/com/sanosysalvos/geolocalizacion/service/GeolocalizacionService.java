package com.sanosysalvos.geolocalizacion.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}