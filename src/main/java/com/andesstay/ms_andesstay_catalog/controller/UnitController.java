package com.andesstay.ms_andesstay_catalog.controller;

import com.andesstay.ms_andesstay_catalog.dto.UnitRequest;
import com.andesstay.ms_andesstay_catalog.entity.Unit;
import com.andesstay.ms_andesstay_catalog.entity.UnitType;
import com.andesstay.ms_andesstay_catalog.service.UnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    // 1. READ (unidades activas; filtros opcionales ?tipo=CABANA&hostal=...&soloDisponibles=true)
    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio','Cliente_Dominio','Auditor')")
    public List<Unit> listar(@RequestParam(required = false) UnitType tipo,
                             @RequestParam(required = false) String hostal,
                             @RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return unitService.listar(tipo, hostal, soloDisponibles);
    }

    // 2. READ (por id, incluye dadas de baja; 404 si no existe)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio','Cliente_Dominio','Auditor')")
    public Unit obtener(@PathVariable Long id) {
        return unitService.buscarPorId(id);
    }

    // 3. CREATE (solo Admin)
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Unit> crear(@Valid @RequestBody UnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.crear(request));
    }

    // 4. UPDATE (solo Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Unit actualizar(@PathVariable Long id, @Valid @RequestBody UnitRequest request) {
        return unitService.actualizar(id, request);
    }

    // 5. DELETE (baja logica: activo=false; solo Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        unitService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Cupos: los llama reservations al confirmar / cancelar / hacer checkout (409 si no hay cupos)
    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio')")
    public Unit reservar(@PathVariable Long id) {
        return unitService.reservarCupo(id);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio')")
    public Unit liberar(@PathVariable Long id) {
        return unitService.liberarCupo(id);
    }
}
