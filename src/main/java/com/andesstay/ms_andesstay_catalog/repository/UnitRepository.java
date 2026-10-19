package com.andesstay.ms_andesstay_catalog.repository;

import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    /**
     * Unidades activas con filtros opcionales (null = sin filtro).
     *
     * @param hostalLower nombre del hostal en minusculas (comparacion sin distinguir mayusculas)
     */
    @Query("""
            select u from Unit u
            where u.activo = true
              and (:tipo is null or u.tipo = :tipo)
              and (:hostalLower is null or lower(u.hostal) = :hostalLower)
              and (:soloDisponibles = false or u.cuposDisponibles > 0)
            order by u.hostal, u.nombre
            """)
    List<Unit> buscarActivas(@Param("tipo") UnitType tipo,
                             @Param("hostalLower") String hostalLower,
                             @Param("soloDisponibles") boolean soloDisponibles);
}
