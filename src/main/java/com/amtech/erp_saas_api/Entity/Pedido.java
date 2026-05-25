package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    /** Usuario que creó/vendió el pedido */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario vendedor;

    /** Usuario asignado para empaquetar (nullable) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embalador_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario embalador;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PedidoDetalle> detalle;

    @PrePersist
    public void prePersist() {
        if (fechaPedido == null) fechaPedido = LocalDateTime.now();
    }

    public enum EstadoPedido {
        PENDIENTE, EMPAQUETANDO, LISTO, ENTREGADO, CANCELADO
    }
}
