package com.amtech.erp_saas_api.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "plan_suscripcion", length = 50)
    @Builder.Default
    private String planSuscripcion = "BASICO";

    @Column(name = "estado")
    @Builder.Default
    private Boolean estado = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Usuario> usuarios;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Categoria> categorias;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Proveedor> proveedores;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Cliente> clientes;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Producto> productos;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "logo_nombre_archivo", length = 255)
    private String logoNombreArchivo;

    @Column(name = "logo_fecha_actualizacion")
    private LocalDateTime logoFechaActualizacion;

    @Column(name = "igv_porcentaje", nullable = false)
    private BigDecimal igvPorcentaje;
}

