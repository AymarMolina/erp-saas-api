package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedidos_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_acordado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAcordado;
}
