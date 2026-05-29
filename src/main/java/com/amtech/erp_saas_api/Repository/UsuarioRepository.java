package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /** Login: busca por empresa + username (unique constraint de la BD) */
    Optional<Usuario> findByEmpresaIdAndUsername(Integer empresaId, String username);

    boolean existsByEmpresaIdAndUsername(Integer empresaId, String username);

    List<Usuario> findByEmpresaId(Integer empresaId);

    List<Usuario> findByEmpresaIdAndEstado(Integer empresaId, Boolean estado);

    /** Usuarios de una empresa con un rol específico */
    List<Usuario> findByEmpresaIdAndRolId(Integer empresaId, Integer rolId);

    /** Embaladores activos de una empresa (para asignar a pedidos) */
    @Query("""
        SELECT u FROM Usuario u
        JOIN u.rol r
        JOIN r.modulos m
        WHERE u.empresa.id = :empresaId
          AND m.nombre = 'LOGISTICA'
          AND u.estado = true
        """)
    List<Usuario> findEmbaladoresActivos(@Param("empresaId") Integer empresaId);

    Optional<Usuario> findByIdAndEmpresaId(Integer id, Integer empresaId);
}
