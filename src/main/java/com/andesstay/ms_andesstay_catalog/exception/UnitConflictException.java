package com.andesstay.ms_andesstay_catalog.exception;

/**
 * Operacion de cupos que no se puede aplicar (sin cupos, unidad inactiva). Se responde 409.
 */
public class UnitConflictException extends RuntimeException {

    public UnitConflictException(String message) {
        super(message);
    }
}
