package com.sanosysalvos.geolocalizacion.service;

import com.sanosysalvos.geolocalizacion.dto.LocationIqResponse;
import com.sanosysalvos.geolocalizacion.dto.ReporteGeograficoResponseDTO;
import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;
import com.sanosysalvos.geolocalizacion.repository.ReporteGeograficoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class GeolocalizacionServiceTest {

    @Mock
    private ReporteGeograficoRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeolocalizacionService service;

    private ReporteGeografico reporteMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(service, "apiUrl", "http://test-url.com/search.php");
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);

        GeometryFactory geometryFactory = new GeometryFactory();
        Point punto = geometryFactory.createPoint(new Coordinate(-70.5953, -33.4156));
        punto.setSRID(4326);

        reporteMock = new ReporteGeografico();
        reporteMock.setId(1);
        reporteMock.setMascotaId(10);
        reporteMock.setUbicacion(punto);
        reporteMock.setRadioKm(5.0);
        reporteMock.setEsActivo(true);
    }

    @Test
    void registrarUbicacion_DebeGuardarYRetornarDTO() {
        LocationIqResponse[] mockResponse = new LocationIqResponse[1];
        LocationIqResponse loc = new LocationIqResponse();
        loc.setLat("-33.4156");
        loc.setLon("-70.5953");
        mockResponse[0] = loc;

        Mockito.lenient().when(repository.findByMascotaId(any())).thenReturn(Optional.empty());
        
        Mockito.when(restTemplate.getForObject(anyString(), eq(LocationIqResponse[].class), any(), any(), any()))
                .thenReturn(mockResponse);
                
        Mockito.when(repository.save(any(ReporteGeografico.class))).thenAnswer(i -> {
            ReporteGeografico r = i.getArgument(0);
            r.setId(1); 
            return r;
        });

        ReporteGeograficoResponseDTO resultado = service.registrarUbicacion(10, "Av Apoquindo 4800");

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals(10, resultado.getMascotaId());
        assertEquals(-33.4156, resultado.getLatitud());
    }

    @Test
    void registrarUbicacion_MascotaYaExiste_DebeLanzarExcepcion() {
        Mockito.when(repository.findByMascotaId(any())).thenReturn(Optional.of(reporteMock));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            service.registrarUbicacion(10, "Av Apoquindo 4800");
        });
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("ya cuenta con una ubicación"));
    }

    @Test
    void obtenerTodos_DebeRetornarListaDeDTOs() {
        Mockito.when(repository.findAll()).thenReturn(List.of(reporteMock));

        List<ReporteGeograficoResponseDTO> resultados = service.obtenerTodos();

        assertEquals(1, resultados.size());
        assertEquals(10, resultados.get(0).getMascotaId());
    }

    @Test
    void obtenerPorId_ExisteId_DebeRetornarDTO() {
        Mockito.when(repository.findById(any())).thenReturn(Optional.of(reporteMock));

        ReporteGeograficoResponseDTO resultado = service.obtenerPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
    }

    @Test
    void eliminarReporte_ExisteId_DebeLlamarAlRepositorio() {
        Mockito.when(repository.findById(any())).thenReturn(Optional.of(reporteMock));

        service.eliminarReporte(1);

        Mockito.verify(repository, Mockito.times(1)).delete(reporteMock);
    }
}