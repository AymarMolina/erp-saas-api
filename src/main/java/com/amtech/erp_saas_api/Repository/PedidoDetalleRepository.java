package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Integer> {

    @Query("SELECT pd FROM PedidoDetalle pd WHERE pd.pedido.id = :pedidoId AND pd.pedido.empresa.id = :empresaId")
    List<PedidoDetalle> findByPedidoIdAndEmpresaId(@Param("pedidoId") Integer pedidoId, @Param("empresaId") Integer empresaId);

    @Query("""
        SELECT pd FROM PedidoDetalle pd 
        WHERE pd.producto.id = :productoId 
          AND pd.pedido.empresa.id = :empresaId 
        ORDER BY pd.pedido.fechaPedido DESC
        """)
    List<PedidoDetalle> findByProductoIdAndEmpresaId(
            @Param("productoId") Integer productoId,
            @Param("empresaId") Integer empresaId
    );

    @Query("""
        SELECT pd FROM PedidoDetalle pd 
        WHERE pd.producto.id = :productoId 
          AND pd.pedido.empresa.id = :empresaId 
          AND (pd.pedido.estado = 'PENDIENTE' OR pd.pedido.estado = 'EMPAQUETANDO')
        """)
    List<PedidoDetalle> findDetallesConStockReservado(
            @Param("productoId") Integer productoId,
            @Param("empresaId") Integer empresaId
    );
}