package com.andesstay.ms_andesstay_catalog.controller;

import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import com.andesstay.ms_andesstay_catalog.exception.ApiExceptionHandler;
import com.andesstay.ms_andesstay_catalog.exception.UnitConflictException;
import com.andesstay.ms_andesstay_catalog.exception.UnitNotFoundException;
import com.andesstay.ms_andesstay_catalog.security.JwtAuthConverter;
import com.andesstay.ms_andesstay_catalog.security.SecurityConfig;
import com.andesstay.ms_andesstay_catalog.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorizacion por rol de cada endpoint y errores JSON (400/404/409).
 */
@WebMvcTest(UnitController.class)
@Import({SecurityConfig.class, JwtAuthConverter.class, ApiExceptionHandler.class})
class UnitControllerSecurityTest {

    private static final String BODY = """
            {"nombre":"Cabana Lago Llanquihue","tipo":"CABANA","hostal":"Hostal Puerto Varas",
             "ubicacion":"Puerto Varas, Los Lagos","capacidad":4,"tarifaNoche":85000,"cuposTotales":3}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnitService unitService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Unit unit = new Unit();
        when(unitService.buscarPorId(anyLong())).thenReturn(unit);
        when(unitService.crear(any())).thenReturn(unit);
        when(unitService.actualizar(anyLong(), any())).thenReturn(unit);
        when(unitService.reservarCupo(anyLong())).thenReturn(unit);
        when(unitService.liberarCupo(anyLong())).thenReturn(unit);
    }

    @ParameterizedTest(name = "GET /api/catalog/units como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,200", "Auditor,200", "Sin_Rol,403"})
    void listar(String rol, int esperado) throws Exception {
        assertStatus(get("/api/catalog/units"), rol, esperado);
    }

    @ParameterizedTest(name = "GET /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,200", "Auditor,200", "Sin_Rol,403"})
    void obtener(String rol, int esperado) throws Exception {
        assertStatus(get("/api/catalog/units/1"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units como {0} -> {1}")
    @CsvSource({"Admin,201", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void crear(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units").contentType(MediaType.APPLICATION_JSON).content(BODY), rol, esperado);
    }

    @ParameterizedTest(name = "PUT /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void actualizar(String rol, int esperado) throws Exception {
        assertStatus(put("/api/catalog/units/1").contentType(MediaType.APPLICATION_JSON).content(BODY), rol, esperado);
    }

    @ParameterizedTest(name = "DELETE /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,204", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void eliminar(String rol, int esperado) throws Exception {
        assertStatus(delete("/api/catalog/units/1"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units/1/reserve como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void reservar(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units/1/reserve"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units/1/release como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void liberar(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units/1/release"), rol, esperado);
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/catalog/units")).andExpect(status().isUnauthorized());
    }

    @Test
    void filtrosLleganAlService() throws Exception {
        mockMvc.perform(get("/api/catalog/units").param("tipo", "DOMO").param("hostal", "Elqui Domos")
                        .param("soloDisponibles", "true").with(usuario("Cliente_Dominio")))
                .andExpect(status().isOk());

        verify(unitService).listar(eq(UnitType.DOMO), eq("Elqui Domos"), eq(true));
    }

    @Test
    void tipoInvalidoResponde400() throws Exception {
        mockMvc.perform(get("/api/catalog/units").param("tipo", "CASTILLO").with(usuario("Admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("CABANA")));
        verify(unitService, never()).listar(any(), any(), anyBoolean());
    }

    @Test
    void bodyInvalidoResponde400ConDetalle() throws Exception {
        String invalido = """
                {"nombre":"Cabana","tipo":"CABANA","hostal":"H","ubicacion":"U",
                 "capacidad":0,"tarifaNoche":85000,"cuposTotales":2,"cuposDisponibles":5}
                """;
        mockMvc.perform(post("/api/catalog/units").contentType(MediaType.APPLICATION_JSON).content(invalido)
                        .with(usuario("Admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "capacidad: debe ser mayor que 0; cuposValidos: cuposDisponibles no puede superar cuposTotales"));
        verify(unitService, never()).crear(any());
    }

    @Test
    void sinCuposResponde409ConMensaje() throws Exception {
        when(unitService.reservarCupo(1L)).thenThrow(new UnitConflictException("La unidad 1 no tiene cupos disponibles"));

        mockMvc.perform(post("/api/catalog/units/1/reserve").with(usuario("Operador_Dominio")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("La unidad 1 no tiene cupos disponibles"));
    }

    @Test
    void unidadInexistenteResponde404() throws Exception {
        when(unitService.buscarPorId(99L)).thenThrow(new UnitNotFoundException(99L));

        mockMvc.perform(get("/api/catalog/units/99").with(usuario("Auditor")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe la unidad con id 99"));
    }

    private void assertStatus(MockHttpServletRequestBuilder request, String rol, int esperado) throws Exception {
        mockMvc.perform(request.with(usuario(rol))).andExpect(status().is(esperado));
    }

    private static RequestPostProcessor usuario(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
