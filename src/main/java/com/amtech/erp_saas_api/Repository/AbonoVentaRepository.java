package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.AbonoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AbonoVentaRepository extends JpaRepository<AbonoVenta, Integer> {
    List<AbonoVenta> findByVentaIdOrderByFechaPagoDesc(Integer ventaId);

    @Query("SELECT a FROM AbonoVenta a WHERE a.venta.empresa.id = :empresaId ORDER BY a.fechaPago DESC")
    List<AbonoVenta> findByEmpresaIdOrderByFechaPagoDesc(@Param("empresaId") Integer empresaId);

    @Query("SELECT a FROM AbonoVenta a WHERE a.venta.empresa.id = :empresaId AND a.fechaPago BETWEEN :inicio AND :fin ORDER BY a.fechaPago DESC")
    List<AbonoVenta> findByEmpresaIdAndFechaPagoBetweenOrderByFechaPagoDesc(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}