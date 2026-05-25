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

    public enum EstadoCompra { COMPLETADA, ANULADA }
}
