package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Compra;
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
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    Optional<Compra> findByIdAndEmpresaId(Integer id, Integer empresaId);


    List<Compra> findByEmpresaIdAndProveedorIdOrderByFechaCompraDesc(Integer empresaId, Integer proveedorId);



    boolean existsByEmpresaIdAndProveedorIdAndComprobante(Integer empresaId, Integer proveedorId, String comprobante);

    List<Compra> findByEmpresaIdAndUsuarioIdOrderByFechaCompraDesc(Integer empresaId, Integer usuarioId);

    @Query("SELECT c FROM Compra c WHERE c.empresa.id = :empresaId AND c.estadoPago != 'PAGADO' AND c.fechaCompra BETWEEN :inicio AND :fin")
    List<Compra> findComprasConDeudaPendientePorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT c FROM Compra c WHERE c.empresa.id = :empresaId AND c.fechaCompra BETWEEN :inicio AND :fin ORDER BY c.fechaCompra DESC")
    List<Compra> findComprasPorRangoDeFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    @Query("SELECT c FROM Compra c WHERE c.empresa.id = :empresaId AND c.saldoPendiente > 0 AND c.estado = 'COMPLETADA' ORDER BY c.fechaCompra DESC")
    List<Compra> findComprasConDeudaPendiente(@Param("empresaId") Integer empresaId);

    // --- MÉTODOS AUTOMÁTICOS (Si tu clase Compra tiene 'private Empresa empresa') ---
    // Si estos dan error al iniciar, cámbialos también por @Query
    List<Compra> findByEmpresaIdOrderByFechaCompraDesc(Integer empresaId);

    // NOTA: Si los de abajo siguen fallando, es porque Spring busca 'empresaId' y no lo encuentra.
    // Cámbialos a @Query igual que los de arriba.

    // KPI: Compras del mes actual
    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compra c WHERE c.empresa.id = :empresaId AND c.estado = 'COMPLETADA' AND YEAR(c.fechaCompra) = :anio AND MONTH(c.fechaCompra) = :mes")
    BigDecimal sumarComprasDelMes(@Param("empresaId") Integer empresaId, @Param("anio") int anio, @Param("mes") int mes);

    // KPI: Total por Pagar
    @Query("SELECT COALESCE(SUM(c.saldoPendiente), 0) FROM Compra c WHERE c.empresa.id = :empresaId AND c.estado = 'COMPLETADA'")
    BigDecimal sumarCuentasPorPagar(@Param("empresaId") Integer empresaId);

    // Vencimientos próximos de facturas de proveedores
    @Query("SELECT c FROM Compra c WHERE c.empresa.id = :empresaId AND c.saldoPendiente > 0 AND c.fechaVencimiento <= :limite ORDER BY c.fechaVencimiento ASC")
    List<Compra> findVencimientosProximos(@Param("empresaId") Integer empresaId, @Param("limite") LocalDate limite);
}