package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);

    List<Rol> findByEstado(Boolean estado);

    @Query("SELECT r FROM Rol r WHERE r.estado = true ORDER BY r.nombre ASC")
    List<Rol> findAllActivos();

    /** Roles que tienen acceso a un módulo específico */
    @Query("SELECT r FROM Rol r JOIN r.modulos m WHERE m.id = :moduloId AND r.estado = true")
    List<Rol> findByModuloId(@Param("moduloId") Integer moduloId);
}
