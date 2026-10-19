package com.andesstay.ms_andesstay_catalog.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitTest {

    @Test
    void reservarDescuentaHastaCeroYNoBajaDeCero() {
        Unit u = unidad(2, 2);

        assertThat(u.reservarCupo()).isTrue();
        assertThat(u.reservarCupo()).isTrue();
        assertThat(u.reservarCupo()).isFalse();
        assertThat(u.getCuposDisponibles()).isZero();
    }

    @Test
    void liberarDevuelveCupoSinSuperarElTotal() {
        Unit u = unidad(2, 1);

        assertThat(u.liberarCupo()).isTrue();
        assertThat(u.liberarCupo()).isFalse();
        assertThat(u.getCuposDisponibles()).isEqualTo(2);
    }

    private static Unit unidad(int totales, int disponibles) {
        Unit u = new Unit();
        u.setCuposTotales(totales);
        u.setCuposDisponibles(disponibles);
        return u;
    }
}
