package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_username_empresa",
                columnNames = {"empresa_id", "username"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Empresa empresa;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rol rol;

    @Column(name = "estado")
    @Builder.Default
    private Boolean estado = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }
}