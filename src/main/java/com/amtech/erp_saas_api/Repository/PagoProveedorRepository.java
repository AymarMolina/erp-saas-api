package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.PagoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoProveedorRepository extends JpaRepository<PagoProveedor, Integer> {

    List<PagoProveedor> findByCompraIdOrderByFechaPagoDesc(Integer compraId);
    // --- NUEVO: Traer todo el historial de la empresa ---
    @Query("SELECT p FROM PagoProveedor p WHERE p.compra.empresa.id = :empresaId ORDER BY p.fechaPago DESC")
    List<PagoProveedor> findByEmpresaIdOrderByFechaPagoDesc(@Param("empresaId") Integer empresaId);

}