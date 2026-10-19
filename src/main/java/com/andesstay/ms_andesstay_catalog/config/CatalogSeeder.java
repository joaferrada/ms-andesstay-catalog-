package com.andesstay.ms_andesstay_catalog.config;

import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import com.andesstay.ms_andesstay_catalog.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga 6 unidades de ejemplo SOLO si la tabla esta vacia (no pisa datos existentes).
 */
@Component
public class CatalogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final UnitRepository unitRepository;

    public CatalogSeeder(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    public void run(String... args) {
        if (unitRepository.count() > 0) {
            return;
        }
        List<Unit> ejemplos = List.of(
                unidad("Cabana Lago Llanquihue", UnitType.CABANA, "Hostal Puerto Varas", "Puerto Varas, Los Lagos", 4, 85000.0, 3),
                unidad("Domo Torres del Paine", UnitType.DOMO, "EcoCamp Patagonia", "Torres del Paine, Magallanes", 2, 180000.0, 6),
                unidad("Lodge Volcan Osorno", UnitType.LODGE, "Lodge Ensenada", "Ensenada, Los Lagos", 6, 150000.0, 2),
                unidad("Habitacion Doble Cerro Castillo", UnitType.HABITACION, "Hostal Aysen", "Villa Cerro Castillo, Aysen", 2, 45000.0, 8),
                unidad("Cabana Bosque Pucon", UnitType.CABANA, "Hostal Villarrica", "Pucon, Araucania", 5, 95000.0, 4),
                unidad("Domo Valle del Elqui", UnitType.DOMO, "Elqui Domos", "Pisco Elqui, Coquimbo", 2, 110000.0, 5));
        unitRepository.saveAll(ejemplos);
        log.info("[SEED] {} unidades de ejemplo cargadas en el catalogo", ejemplos.size());
    }

    private static Unit unidad(String nombre, UnitType tipo, String hostal, String ubicacion,
                               int capacidad, double tarifaNoche, int cupos) {
        Unit u = new Unit();
        u.setNombre(nombre);
        u.setTipo(tipo);
        u.setHostal(hostal);
        u.setUbicacion(ubicacion);
        u.setCapacidad(capacidad);
        u.setTarifaNoche(tarifaNoche);
        u.setCuposTotales(cupos);
        u.setCuposDisponibles(cupos);
        u.setActivo(true);
        return u;
    }
}
