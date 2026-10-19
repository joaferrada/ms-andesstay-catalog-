package com.andesstay.ms_andesstay_catalog.entity;

import jakarta.persistence.*;

/**
 * Unidad de alojamiento del catalogo.
 *
 * Invariante de cupos: 0 <= cuposDisponibles <= cuposTotales. Los cambios de cupo pasan por
 * {@link #reservarCupo()} y {@link #liberarCupo()}; @Version evita que dos confirmaciones
 * simultaneas descuenten el mismo cupo.
 */
@Entity
@Table(name = "CATALOG_UNITS")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitType tipo;

    @Column(nullable = false, length = 150)
    private String hostal;

    @Column(nullable = false, length = 200)
    private String ubicacion;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Double tarifaNoche;

    @Column(nullable = false)
    private Integer cuposTotales;

    @Column(nullable = false)
    private Integer cuposDisponibles;

    @Column(nullable = false)
    private Boolean activo = true;

    @Version
    private Long version;

    public Unit() {}

    /** @return true si se desconto un cupo; false si no quedaban. */
    public boolean reservarCupo() {
        if (cuposDisponibles <= 0) {
            return false;
        }
        cuposDisponibles--;
        return true;
    }

    /** Devuelve un cupo sin superar cuposTotales. @return true si el cupo efectivamente se devolvio. */
    public boolean liberarCupo() {
        if (cuposDisponibles >= cuposTotales) {
            return false;
        }
        cuposDisponibles++;
        return true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public UnitType getTipo() { return tipo; }
    public void setTipo(UnitType tipo) { this.tipo = tipo; }

    public String getHostal() { return hostal; }
    public void setHostal(String hostal) { this.hostal = hostal; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Double getTarifaNoche() { return tarifaNoche; }
    public void setTarifaNoche(Double tarifaNoche) { this.tarifaNoche = tarifaNoche; }

    public Integer getCuposTotales() { return cuposTotales; }
    public void setCuposTotales(Integer cuposTotales) { this.cuposTotales = cuposTotales; }

    public Integer getCuposDisponibles() { return cuposDisponibles; }
    public void setCuposDisponibles(Integer cuposDisponibles) { this.cuposDisponibles = cuposDisponibles; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Long getVersion() { return version; }
}
