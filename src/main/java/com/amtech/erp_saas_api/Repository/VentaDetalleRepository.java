package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {

    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.venta.id = :ventaId AND vd.venta.empresa.id = :empresaId")
    List<VentaDetalle> findByVentaIdAndEmpresaId(@Param("ventaId") Integer ventaId, @Param("empresaId") Integer empresaId);

    @Query("""
        SELECT vd FROM VentaDetalle vd 
        WHERE vd.lote.producto.id = :productoId 
          AND vd.venta.empresa.id = :empresaId 
          AND vd.venta.estado = 'COMPLETADA'
        ORDER BY vd.venta.fechaVenta DESC
        """)
    List<VentaDetalle> findVentasPorProducto(
            @Param("productoId") Integer productoId,
            @Param("empresaId") Integer empresaId
    );

     @Query("""
        SELECT vd FROM VentaDetalle vd 
        WHERE vd.lote.id = :loteId 
          AND vd.venta.empresa.id = :empresaId
          AND vd.venta.estado = 'COMPLETADA'
        ORDER BY vd.venta.fechaVenta DESC
        """)
    List<VentaDetalle> findTrazabilidadPorLote(
            @Param("loteId") Integer loteId,
            @Param("empresaId") Integer empresaId
    );
}