package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    Optional<Proveedor> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Proveedor> findByEmpresaId(Integer empresaId);

    List<Proveedor> findByEmpresaIdAndEstadoTrueOrderByRazonSocialAsc(Integer empresaId);

    Optional<Proveedor> findByEmpresaIdAndDocumentoIdentidad(Integer empresaId, String documentoIdentidad);

    boolean existsByEmpresaIdAndDocumentoIdentidad(Integer empresaId, String documentoIdentidad);

    List<Proveedor> findByEmpresaIdAndRazonSocialContainingIgnoreCaseAndEstadoTrue(Integer empresaId, String razonSocial);
}