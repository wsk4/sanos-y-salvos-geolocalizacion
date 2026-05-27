package com.sanosysalvos.geolocalizacion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanosysalvos.geolocalizacion.model.ReporteGeografico;

@Repository
public interface ReporteGeograficoRepository extends JpaRepository<ReporteGeografico, Integer> {
    Optional<ReporteGeografico> findByMascotaId(Integer mascotaId);
}
