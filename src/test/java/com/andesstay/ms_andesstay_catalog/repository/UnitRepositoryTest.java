package com.andesstay.ms_andesstay_catalog.repository;

import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UnitRepositoryTest {

    @Autowired
    private UnitRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(unidad("Cabana Lago", UnitType.CABANA, "Hostal Puerto Varas", 2, true));
        repository.save(unidad("Cabana Llena", UnitType.CABANA, "Hostal Puerto Varas", 0, true));
        repository.save(unidad("Domo Elqui", UnitType.DOMO, "Elqui Domos", 3, true));
        repository.save(unidad("Domo Baja", UnitType.DOMO, "Elqui Domos", 3, false));
    }

    @Test
    void sinFiltrosDevuelveSoloActivas() {
        assertThat(repository.buscarActivas(null, null, false))
                .extracting(Unit::getNombre)
                .containsExactlyInAnyOrder("Cabana Lago", "Cabana Llena", "Domo Elqui");
    }

    @Test
    void filtraPorTipoHostalYDisponibilidad() {
        assertThat(repository.buscarActivas(UnitType.DOMO, null, false)).extracting(Unit::getNombre)
                .containsExactly("Domo Elqui");
        assertThat(repository.buscarActivas(null, "hostal puerto varas", false)).hasSize(2);
        assertThat(repository.buscarActivas(UnitType.CABANA, null, true)).extracting(Unit::getNombre)
                .containsExactly("Cabana Lago");
    }

    private static Unit unidad(String nombre, UnitType tipo, String hostal, int disponibles, boolean activo) {
        Unit u = new Unit();
        u.setNombre(nombre);
        u.setTipo(tipo);
        u.setHostal(hostal);
        u.setUbicacion("Chile");
        u.setCapacidad(2);
        u.setTarifaNoche(50000.0);
        u.setCuposTotales(3);
        u.setCuposDisponibles(disponibles);
        u.setActivo(activo);
        return u;
    }
}
