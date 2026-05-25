package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "proveedores",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_proveedor_empresa",
                columnNames = {"empresa_id", "documento_identidad"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "documento_identidad", nullable = false, length = 20)
    private String documentoIdentidad;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "estado")
    @Builder.Default
    private Boolean estado = true;
}
