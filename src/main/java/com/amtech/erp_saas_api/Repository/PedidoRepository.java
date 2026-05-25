package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Pedido> findByEmpresaIdOrderByFechaPedidoDesc(Integer empresaId);

    @Query("""
        SELECT p FROM Pedido p 
        WHERE p.empresa.id = :empresaId 
          AND p.estado = :estado 
        ORDER BY p.fechaPedido ASC
        """)
    List<Pedido> findPedidosParaFulfillment(
            @Param("empresaId") Integer empresaId,
            @Param("estado") Pedido.EstadoPedido estado
    );

    List<Pedido> findByEmpresaIdAndEmbaladorIdAndEstadoOrderByFechaPedidoAsc(
            Integer empresaId,
            Integer embaladorId,
            Pedido.EstadoPedido estado
    );

    List<Pedido> findByEmpresaIdAndVendedorIdOrderByFechaPedidoDesc(Integer empresaId, Integer vendedorId);

    List<Pedido> findByEmpresaIdAndClienteIdOrderByFechaPedidoDesc(Integer empresaId, Integer clienteId);

    @Query("""
        SELECT p FROM Pedido p 
        WHERE p.empresa.id = :empresaId 
          AND p.fechaPedido BETWEEN :inicio AND :fin 
        ORDER BY p.fechaPedido DESC
        """)
    List<Pedido> findPedidosPorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}