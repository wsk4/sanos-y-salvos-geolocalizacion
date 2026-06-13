package com.sanosysalvos.geolocalizacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanosysalvos.geolocalizacion.dto.ReporteGeograficoResponseDTO;
import com.sanosysalvos.geolocalizacion.dto.UbicacionRequestDTO;
import com.sanosysalvos.geolocalizacion.service.GeolocalizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeolocalizacionController.class)
class GeolocalizacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeolocalizacionService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ReporteGeograficoResponseDTO reporteDTO;

    @BeforeEach
    void setUp() {
        reporteDTO = ReporteGeograficoResponseDTO.builder()
                .id(1)
                .mascotaId(10)
                .latitud(-33.4156)
                .longitud(-70.5953)
                .radioKm(5.0)
                .esActivo(true)
                .build();
    }

    @Test
    void registrarUbicacion_DebeRetornarEstado201() throws Exception {
        UbicacionRequestDTO request = new UbicacionRequestDTO();
        request.setMascotaId(10);
        request.setDireccion("Av Apoquindo 4800, Las Condes");

        Mockito.when(service.registrarUbicacion(anyInt(), anyString())).thenReturn(reporteDTO);

        mockMvc.perform(post("/api/v1/geolocalizacion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mascotaId").value(10));
    }

    @Test
    void listarUbicaciones_DebeRetornarListaYEstado200() throws Exception {
        Mockito.when(service.obtenerTodos()).thenReturn(List.of(reporteDTO));

        mockMvc.perform(get("/api/v1/geolocalizacion")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorId_DebeRetornarDTOYEstado200() throws Exception {
        Mockito.when(service.obtenerPorId(1)).thenReturn(reporteDTO);

        mockMvc.perform(get("/api/v1/geolocalizacion/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarReporte_DebeRetornarEstado204() throws Exception {
        Mockito.doNothing().when(service).eliminarReporte(1);

        mockMvc.perform(delete("/api/v1/geolocalizacion/1"))
                .andExpect(status().isNoContent());
    }
}