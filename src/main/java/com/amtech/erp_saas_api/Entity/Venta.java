package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    /** Puede ser null (venta anónima / consumidor final) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "comprobante", nullable = false, length = 50)
    private String comprobante;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    // ── Campos añadidos en el ALTER TABLE (facturación electrónica) ──
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    @Builder.Default
    private TipoComprobante tipoComprobante = TipoComprobante.TICKET;

    @Column(name = "subtotal_sin_impuesto", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotalSinImpuesto = BigDecimal.ZERO;

    @Column(name = "impuesto_total", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal impuestoTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "venta", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<VentaDetalle> detalle;

    @PrePersist
    public void prePersist() {
        if (fechaVenta == null) fechaVenta = LocalDateTime.now();
    }

    public enum EstadoVenta { COMPLETADA, ANULADA }

    public enum TipoComprobante { TICKET, BOLETA, FACTURA }

    // Agrega esto debajo de "estado" y antes de las listas/relaciones:

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_pago", nullable = false, length = 20)
    @Builder.Default
    private CondicionPago condicionPago = CondicionPago.CONTADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estadoPago = EstadoPago.PAGADO;

    @Column(name = "fecha_vencimiento")
    private java.time.LocalDate fechaVencimiento;

    @Column(name = "saldo_pendiente", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    // Al final del archivo, junto a tus otros enums, agrega estos:
    public enum CondicionPago { CONTADO, CREDITO }
    public enum EstadoPago { PAGADO, PENDIENTE, PARCIAL }
}