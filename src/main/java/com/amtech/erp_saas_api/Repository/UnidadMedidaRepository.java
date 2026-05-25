package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {

    Optional<UnidadMedida> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<UnidadMedida> findByEmpresaIdOrderByNombreAsc(Integer empresaId);

    boolean existsByEmpresaIdAndNombreIgnoreCase(Integer empresaId, String nombre);

    boolean existsByEmpresaIdAndAbreviaturaIgnoreCase(Integer empresaId, String abreviatura);

    List<UnidadMedida> findByEmpresaId(Integer empresaId);

    boolean existsByEmpresaIdAndNombre(Integer empresaId, String nombre);
}