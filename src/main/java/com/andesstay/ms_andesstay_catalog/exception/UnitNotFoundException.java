package com.andesstay.ms_andesstay_catalog.exception;

public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException(Long id) {
        super("No existe la unidad con id " + id);
    }
}
