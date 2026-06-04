package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @Column(name = "comprobante", nullable = false, length = 50)
    private String comprobante;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoCompra estado = EstadoCompra.COMPLETADA;

    @OneToMany(mappedBy = "compra", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Lote> lotes;

    @PrePersist
    public void prePersist() {
        if (fechaCompra == null) fechaCompra = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_pago", nullable = false)
    private CondicionPago condicionPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false)
    private EstadoPago estadoPago;

    @Column(name = "fecha_vencimiento")
    private java.time.LocalDate fechaVencimiento;

    @Column(name = "saldo_pendiente", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoPendiente;

    // Relación para traer todos los pagos que se le han hecho a esta compra
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<PagoProveedor> pagos;

    @Column(name = "subtotal_sin_impuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalSinImpuesto;

    @Column(name = "impuesto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal impuestoTotal;

    // --- ENUMS PARA COMPRAS ---

    public enum EstadoCompra {
        COMPLETADA, ANULADA
    }

    public enum CondicionPago {
        CONTADO, CREDITO
    }

    public enum EstadoPago {
        PAGADO, PENDIENTE, PARCIAL
    }
}
