package com.andesstay.ms_andesstay_catalog.dto;

import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Body de POST y PUT /api/catalog/units. id y version nunca se toman del body.
 *
 * @param cuposDisponibles opcional: al crear vale cuposTotales; al actualizar se conserva el actual
 *                         (recortado a cuposTotales si este baja)
 * @param activo           opcional, por defecto true
 */
public record UnitRequest(
        @NotBlank(message = "es obligatorio") @Size(max = 150) String nombre,
        @NotNull(message = "es obligatorio") UnitType tipo,
        @NotBlank(message = "es obligatorio") @Size(max = 150) String hostal,
        @NotBlank(message = "es obligatorio") @Size(max = 200) String ubicacion,
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que 0") Integer capacidad,
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que 0") Double tarifaNoche,
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que 0") Integer cuposTotales,
        @PositiveOrZero(message = "no puede ser negativo") Integer cuposDisponibles,
        Boolean activo) {

    @AssertTrue(message = "cuposDisponibles no puede superar cuposTotales")
    public boolean isCuposValidos() {
        return cuposDisponibles == null || cuposTotales == null || cuposDisponibles <= cuposTotales;
    }
}
