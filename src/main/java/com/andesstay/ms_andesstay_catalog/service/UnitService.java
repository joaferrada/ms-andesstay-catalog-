package com.andesstay.ms_andesstay_catalog.service;

import com.andesstay.ms_andesstay_catalog.dto.UnitRequest;
import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import com.andesstay.ms_andesstay_catalog.exception.UnitConflictException;
import com.andesstay.ms_andesstay_catalog.exception.UnitNotFoundException;
import com.andesstay.ms_andesstay_catalog.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Logica del catalogo: CRUD de unidades (baja logica) y control de cupos.
 */
@Service
public class UnitService {

    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    /** Solo unidades activas. Filtros opcionales (null = sin filtro). */
    @Transactional(readOnly = true)
    public List<Unit> listar(UnitType tipo, String hostal, boolean soloDisponibles) {
        String hostalLower = hostal == null || hostal.isBlank() ? null : hostal.trim().toLowerCase(Locale.ROOT);
        return unitRepository.buscarActivas(tipo, hostalLower, soloDisponibles);
    }

    /** Incluye unidades dadas de baja (activo=false). */
    @Transactional(readOnly = true)
    public Unit buscarPorId(Long id) {
        return unitRepository.findById(id).orElseThrow(() -> new UnitNotFoundException(id));
    }

    @Transactional
    public Unit crear(UnitRequest request) {
        Unit unit = new Unit();
        aplicar(unit, request);
        unit.setCuposDisponibles(request.cuposDisponibles() != null
                ? request.cuposDisponibles()
                : request.cuposTotales());
        return unitRepository.save(unit);
    }

    @Transactional
    public Unit actualizar(Long id, UnitRequest request) {
        Unit unit = buscarPorId(id);
        aplicar(unit, request);
        int disponibles = request.cuposDisponibles() != null
                ? request.cuposDisponibles()
                : unit.getCuposDisponibles();
        unit.setCuposDisponibles(Math.min(disponibles, request.cuposTotales()));
        return unitRepository.save(unit);
    }

    /** Baja logica: la unidad deja de listarse y no acepta reservas, pero se conserva. */
    @Transactional
    public void desactivar(Long id) {
        Unit unit = buscarPorId(id);
        unit.setActivo(false);
        unitRepository.save(unit);
    }

    /**
     * Descuenta un cupo (lo llama reservations al confirmar).
     *
     * @throws UnitConflictException 409 si la unidad esta inactiva o no le quedan cupos
     */
    @Transactional
    public Unit reservarCupo(Long id) {
        Unit unit = buscarPorId(id);
        if (!Boolean.TRUE.equals(unit.getActivo())) {
            throw new UnitConflictException("La unidad " + id + " (" + unit.getNombre() + ") esta inactiva");
        }
        if (!unit.reservarCupo()) {
            throw new UnitConflictException("La unidad " + id + " (" + unit.getNombre()
                    + ") no tiene cupos disponibles (0 de " + unit.getCuposTotales() + ")");
        }
        return unitRepository.save(unit);
    }

    /**
     * Devuelve un cupo (cancelacion o checkout). Si ya estaban todos disponibles no hace nada:
     * cuposDisponibles nunca supera cuposTotales.
     */
    @Transactional
    public Unit liberarCupo(Long id) {
        Unit unit = buscarPorId(id);
        return unit.liberarCupo() ? unitRepository.save(unit) : unit;
    }

    private static void aplicar(Unit unit, UnitRequest request) {
        unit.setNombre(request.nombre().trim());
        unit.setTipo(request.tipo());
        unit.setHostal(request.hostal().trim());
        unit.setUbicacion(request.ubicacion().trim());
        unit.setCapacidad(request.capacidad());
        unit.setTarifaNoche(request.tarifaNoche());
        unit.setCuposTotales(request.cuposTotales());
        unit.setActivo(request.activo() == null || request.activo());
    }
}
