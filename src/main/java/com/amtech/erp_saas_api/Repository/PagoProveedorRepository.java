package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.PagoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoProveedorRepository extends JpaRepository<PagoProveedor, Integer> {

    List<PagoProveedor> findByCompraIdOrderByFechaPagoDesc(Integer compraId);

    @Query("SELECT p FROM PagoProveedor p WHERE p.compra.empresa.id = :empresaId ORDER BY p.fechaPago DESC")
    List<PagoProveedor> findByEmpresaIdOrderByFechaPagoDesc(@Param("empresaId") Integer empresaId);

    @Query("SELECT p FROM PagoProveedor p WHERE p.compra.empresa.id = :empresaId AND p.fechaPago BETWEEN :inicio AND :fin ORDER BY p.fechaPago DESC")
    List<PagoProveedor> findByEmpresaIdAndFechaPagoBetweenOrderByFechaPagoDesc(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}