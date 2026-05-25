package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    Optional<Compra> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Compra> findByEmpresaIdOrderByFechaCompraDesc(Integer empresaId);

    List<Compra> findByEmpresaIdAndProveedorIdOrderByFechaCompraDesc(Integer empresaId, Integer proveedorId);

    @Query("""
        SELECT c FROM Compra c 
        WHERE c.empresa.id = :empresaId 
          AND c.fechaCompra BETWEEN :inicio AND :fin 
        ORDER BY c.fechaCompra DESC
        """)
    List<Compra> findComprasPorRangoDeFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    boolean existsByEmpresaIdAndProveedorIdAndComprobante(Integer empresaId, Integer proveedorId, String comprobante);

    List<Compra> findByEmpresaIdAndUsuarioIdOrderByFechaCompraDesc(Integer empresaId, Integer usuarioId);
}