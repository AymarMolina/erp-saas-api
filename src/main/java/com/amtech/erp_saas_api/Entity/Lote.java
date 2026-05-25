package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Compra compra;

    @Column(name = "codigo_lote", length = 50)
    private String codigoLote;

    @Column(name = "fecha_fabricacion")
    private LocalDate fechaFabricacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "cantidad_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoLote estado = EstadoLote.ACTIVO;

    public enum EstadoLote { ACTIVO, AGOTADO, VENCIDO }
}
