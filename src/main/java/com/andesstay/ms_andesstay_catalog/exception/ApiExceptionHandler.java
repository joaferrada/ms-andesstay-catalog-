package com.andesstay.ms_andesstay_catalog.exception;

import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce excepciones a respuestas JSON {timestamp, status, error, message}.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnitNotFoundException.class)
    public ResponseEntity<Map<String, Object>> noEncontrada(UnitNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnitConflictException.class)
    public ResponseEntity<Map<String, Object>> conflicto(UnitConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Dos operaciones de cupo simultaneas sobre la misma unidad (@Version). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> concurrencia(OptimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT, "La unidad fue modificada por otra operacion; reintente");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> entradaInvalida(Exception ex) {
        return error(HttpStatus.BAD_REQUEST,
                "Entrada invalida. Tipos validos: " + Arrays.toString(UnitType.values()));
    }

    @ExceptionHandler({CannotCreateTransactionException.class, DataAccessResourceFailureException.class})
    public ResponseEntity<Map<String, Object>> baseDeDatosNoDisponible(Exception ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Base de datos del catalogo no disponible");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
