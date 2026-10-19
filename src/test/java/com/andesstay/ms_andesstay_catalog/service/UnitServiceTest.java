package com.andesstay.ms_andesstay_catalog.service;

import com.andesstay.ms_andesstay_catalog.dto.UnitRequest;
import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import com.andesstay.ms_andesstay_catalog.exception.UnitConflictException;
import com.andesstay.ms_andesstay_catalog.exception.UnitNotFoundException;
import com.andesstay.ms_andesstay_catalog.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitServiceTest {

    private UnitRepository repository;
    private UnitService service;

    @BeforeEach
    void setUp() {
        repository = mock(UnitRepository.class);
        service = new UnitService(repository);
        when(repository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void reservarDescuentaUnCupo() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(3, 2, true)));

        assertThat(service.reservarCupo(1L).getCuposDisponibles()).isEqualTo(1);
    }

    @Test
    void reservarSinCuposLanza409YNoGuarda() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(3, 0, true)));

        assertThatThrownBy(() -> service.reservarCupo(1L))
                .isInstanceOf(UnitConflictException.class)
                .hasMessageContaining("no tiene cupos disponibles");
        verify(repository, never()).save(any());
    }

    @Test
    void reservarUnidadInactivaLanza409() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(3, 3, false)));

        assertThatThrownBy(() -> service.reservarCupo(1L))
                .isInstanceOf(UnitConflictException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void liberarNoSuperaElTotal() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(3, 3, true)));

        assertThat(service.liberarCupo(1L).getCuposDisponibles()).isEqualTo(3);
        verify(repository, never()).save(any());
    }

    @Test
    void liberarDevuelveUnCupo() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(3, 1, true)));

        assertThat(service.liberarCupo(1L).getCuposDisponibles()).isEqualTo(2);
    }

    @Test
    void unidadInexistenteLanza404() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservarCupo(9L)).isInstanceOf(UnitNotFoundException.class);
    }

    @Test
    void crearSinCuposDisponiblesPartenTodosLibres() {
        Unit creada = service.crear(request(4, null));

        assertThat(creada.getCuposDisponibles()).isEqualTo(4);
        assertThat(creada.getActivo()).isTrue();
    }

    @Test
    void actualizarRecortaDisponiblesSiBajaElTotal() {
        when(repository.findById(1L)).thenReturn(Optional.of(unidad(6, 5, true)));

        assertThat(service.actualizar(1L, request(2, null)).getCuposDisponibles()).isEqualTo(2);
    }

    @Test
    void eliminarEsBajaLogica() {
        Unit u = unidad(3, 3, true);
        when(repository.findById(1L)).thenReturn(Optional.of(u));

        service.desactivar(1L);

        assertThat(u.getActivo()).isFalse();
        verify(repository).save(u);
        verify(repository, never()).delete(any());
    }

    private static UnitRequest request(int totales, Integer disponibles) {
        return new UnitRequest("Cabana Bosque Pucon", UnitType.CABANA, "Hostal Villarrica", "Pucon, Araucania",
                5, 95000.0, totales, disponibles, null);
    }

    private static Unit unidad(int totales, int disponibles, boolean activo) {
        Unit u = new Unit();
        u.setId(1L);
        u.setNombre("Cabana Bosque Pucon");
        u.setCuposTotales(totales);
        u.setCuposDisponibles(disponibles);
        u.setActivo(activo);
        return u;
    }
}
