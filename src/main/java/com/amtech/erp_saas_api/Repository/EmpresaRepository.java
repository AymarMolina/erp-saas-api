package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Empresa> findByEstado(Boolean estado);

    List<Empresa> findByPlanSuscripcion(String planSuscripcion);

    @Query("SELECT e FROM Empresa e WHERE e.estado = true ORDER BY e.razonSocial ASC")
    List<Empresa> findAllActivas();
}