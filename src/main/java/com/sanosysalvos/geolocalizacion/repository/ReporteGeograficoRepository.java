package com.sanosysalvos.geolocalizacion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;

@Repository
public interface ReporteGeograficoRepository extends JpaRepository<ReporteGeografico, Integer> {

    // NUEVO: obtiene el reporte más reciente de una mascota (por si tiene múltiples reportes)
    Optional<ReporteGeografico> findFirstByMascotaIdOrderByIdDesc(Integer mascotaId);
}