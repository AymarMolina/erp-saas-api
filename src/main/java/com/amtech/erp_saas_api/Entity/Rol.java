package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_modulo",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "modulo_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Modulo> modulos;

    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Usuario> usuarios;
}
