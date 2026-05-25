package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Cliente> findByEmpresaId(Integer empresaId);

    Optional<Cliente> findByEmpresaIdAndDocumentoIdentidad(Integer empresaId, String documentoIdentidad);

    boolean existsByEmpresaIdAndDocumentoIdentidad(Integer empresaId, String documentoIdentidad);

    List<Cliente> findByEmpresaIdAndNombreCompletoContainingIgnoreCase(Integer empresaId, String nombreCompleto);
}