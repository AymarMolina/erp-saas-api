package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lote lote;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "motivo", nullable = false, length = 100)
    private String motivo;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "saldo_lote", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoLote;

    @PrePersist
    public void prePersist() {
        if (fechaMovimiento == null) fechaMovimiento = LocalDateTime.now();
    }

    public enum TipoMovimiento { ENTRADA, SALIDA, AJUSTE }
}
