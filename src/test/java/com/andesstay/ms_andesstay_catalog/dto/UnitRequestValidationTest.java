package com.andesstay.ms_andesstay_catalog.dto;

import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UnitRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void unidadCompletaEsValida() {
        assertThat(validator.validate(request(4, 85000.0, 3, 2))).isEmpty();
        assertThat(validator.validate(request(4, 85000.0, 3, null))).isEmpty();
    }

    @Test
    void capacidadTarifaYCuposDebenSerMayoresQueCero() {
        assertThat(campos(request(0, 0.0, 0, null)))
                .containsExactlyInAnyOrder("capacidad", "tarifaNoche", "cuposTotales");
    }

    @Test
    void cuposDisponiblesEntreCeroYElTotal() {
        assertThat(campos(request(4, 85000.0, 3, -1))).containsExactly("cuposDisponibles");
        assertThat(campos(request(4, 85000.0, 3, 4))).containsExactly("cuposValidos");
    }

    @Test
    void camposDeTextoYTipoObligatorios() {
        UnitRequest vacio = new UnitRequest(" ", null, "", null, 1, 1.0, 1, null, null);
        assertThat(campos(vacio)).containsExactlyInAnyOrder("nombre", "tipo", "hostal", "ubicacion");
    }

    private Set<String> campos(UnitRequest request) {
        Set<ConstraintViolation<UnitRequest>> errores = validator.validate(request);
        return errores.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    private static UnitRequest request(int capacidad, double tarifa, int totales, Integer disponibles) {
        return new UnitRequest("Cabana Lago Llanquihue", UnitType.CABANA, "Hostal Puerto Varas",
                "Puerto Varas, Los Lagos", capacidad, tarifa, totales, disponibles, null);
    }
}
