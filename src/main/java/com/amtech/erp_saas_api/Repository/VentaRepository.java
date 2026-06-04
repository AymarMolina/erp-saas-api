package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {


    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId AND v.estadoPago != 'PAGADO' AND v.fechaVenta BETWEEN :inicio AND :fin")
    List<Venta> findVentasConDeudaPendientePorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId AND v.fechaVenta BETWEEN :inicio AND :fin ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasPorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId ORDER BY v.fechaVenta DESC")
    List<Venta> findByEmpresaIdOrderByFechaVentaDesc(@Param("empresaId") Integer empresaId);

    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId")
    List<Venta> findByEmpresaId(@Param("empresaId") Integer empresaId);

    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId AND v.cliente.id = :clienteId ORDER BY v.fechaVenta DESC")
    List<Venta> findByEmpresaIdAndClienteIdOrderByFechaVentaDesc(@Param("empresaId") Integer empresaId, @Param("clienteId") Integer clienteId);

    Optional<Venta> findByIdAndEmpresaId(Integer id, Integer empresaId);
    Optional<Venta> findByEmpresaIdAndComprobante(Integer empresaId, String comprobante);
    boolean existsByEmpresaIdAndComprobante(Integer empresaId, String comprobante);

    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId AND v.condicionPago = 'CREDITO' AND v.estadoPago != 'PAGADO' ORDER BY v.fechaVencimiento ASC")
    List<Venta> findVentasConDeudaPendiente(@Param("empresaId") Integer empresaId);

    @Query("""
        SELECT v FROM Venta v 
        WHERE v.empresa.id = :empresaId 
          AND v.usuario.id = :usuarioId 
          AND v.fechaVenta BETWEEN :inicio AND :fin 
          AND v.estado = 'COMPLETADA'
        ORDER BY v.fechaVenta DESC
        """)
    List<Venta> findVentasParaCuadreDeCaja(
            @Param("empresaId") Integer empresaId,
            @Param("usuarioId") Integer usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // KPI: Ventas del mes actual (COMPLETADAS)
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.empresa.id = :empresaId AND v.estado = 'COMPLETADA' AND YEAR(v.fechaVenta) = :anio AND MONTH(v.fechaVenta) = :mes")
    BigDecimal sumarVentasDelMes(@Param("empresaId") Integer empresaId, @Param("anio") int anio, @Param("mes") int mes);

    // KPI: Total por Cobrar
    @Query("SELECT COALESCE(SUM(v.saldoPendiente), 0) FROM Venta v WHERE v.empresa.id = :empresaId AND v.estado = 'COMPLETADA'")
    BigDecimal sumarCuentasPorCobrar(@Param("empresaId") Integer empresaId);

    // Últimas 5 ventas
    List<Venta> findTop5ByEmpresaIdAndEstadoOrderByFechaVentaDesc(Integer empresaId, Venta.EstadoVenta estado);

    // Vencimientos próximos (Próximos 5 días o ya vencidos)
    @Query("SELECT v FROM Venta v WHERE v.empresa.id = :empresaId AND v.saldoPendiente > 0 AND v.fechaVencimiento <= :limite ORDER BY v.fechaVencimiento ASC")
    List<Venta> findVencimientosProximos(@Param("empresaId") Integer empresaId, @Param("limite") LocalDate limite);
}
