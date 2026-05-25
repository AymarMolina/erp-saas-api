package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidades_medida")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "abreviatura", nullable = false, length = 10)
    private String abreviatura;

    @Column(name = "permite_fraccion")
    @Builder.Default
    private Boolean permiteFraccion = false;
}
