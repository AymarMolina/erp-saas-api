package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "modulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "estado")
    @Builder.Default
    private Boolean estado = true;

    @ManyToMany(mappedBy = "modulos", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Rol> roles;
}
