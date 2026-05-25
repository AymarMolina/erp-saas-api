package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "productos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_codigo_empresa",
                columnNames = {"empresa_id", "codigo_barras"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "codigo_barras", length = 100)
    private String codigoBarras;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UnidadMedida unidadMedida;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /**
     * Stock físico: cantidad real en almacén.
     * Modificado automáticamente por triggers en BD.
     * No modificar directamente desde Java.
     */
    @Column(name = "stock_fisico", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockFisico = BigDecimal.ZERO;

    /**
     * Stock reservado: comprometido en pedidos aún no facturados.
     * Modificado automáticamente por triggers en BD.
     */
    @Column(name = "stock_reservado", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockReservado = BigDecimal.ZERO;

    @Column(name = "estado")
    @Builder.Default
    private Boolean estado = true;

    /**
     * Stock disponible = stock_fisico - stock_reservado (calculado, no persistido)
     */
    @Transient
    public BigDecimal getStockDisponible() {
        return stockFisico.subtract(stockReservado);
    }
}