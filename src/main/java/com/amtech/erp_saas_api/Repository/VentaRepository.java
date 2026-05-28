package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Venta> findByEmpresaIdOrderByFechaVentaDesc(Integer empresaId);

    Optional<Venta> findByEmpresaIdAndComprobante(Integer empresaId, String comprobante);

    List<Venta> findByEmpresaIdAndClienteIdOrderByFechaVentaDesc(Integer empresaId, Integer clienteId);

    List<Venta> findByEmpresaId(Integer empresaId);
    
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

    @Query("""
        SELECT v FROM Venta v 
        WHERE v.empresa.id = :empresaId 
          AND v.fechaVenta BETWEEN :inicio AND :fin 
        ORDER BY v.fechaVenta DESC
        """)
    List<Venta> findVentasPorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    boolean existsByEmpresaIdAndComprobante(Integer empresaId, String comprobante);
}
