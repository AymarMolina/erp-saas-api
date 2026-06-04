package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Optional<Producto> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Producto> findByEmpresaId(Integer empresaId);

    Optional<Producto> findByEmpresaIdAndCodigoBarrasAndEstadoTrue(Integer empresaId, String codigoBarras);

    List<Producto> findByEmpresaIdAndNombreContainingIgnoreCaseAndEstadoTrue(Integer empresaId, String nombre);

    boolean existsByEmpresaIdAndCodigoBarras(Integer empresaId, String codigoBarras);

    List<Producto> findByEmpresaIdAndCategoriaIdAndEstadoTrue(Integer empresaId, Integer categoriaId);

    List<Producto> findByEmpresaIdAndEstadoTrue(Integer empresaId);
    
    @Query("""
        SELECT p FROM Producto p 
        WHERE p.empresa.id = :empresaId 
          AND p.estado = true 
          AND p.stockFisico <= :umbral
        ORDER BY p.stockFisico ASC
        """)
    List<Producto> findProductosConStockBajo(@Param("empresaId") Integer empresaId, @Param("umbral") BigDecimal umbral);

    @Modifying
    @Query("UPDATE Producto p SET p.stockReservado = p.stockReservado - :cantidad WHERE p.id = :productoId")
    void restarStockReservado(@Param("productoId") Integer productoId, @Param("cantidad") BigDecimal cantidad);

    @Query("SELECT p FROM Producto p WHERE p.empresa.id = :empresaId AND p.stockFisico <= 10 AND p.estado = true ORDER BY p.stockFisico ASC")
    List<Producto> findTop5StockCritico(@Param("empresaId") Integer empresaId, org.springframework.data.domain.Pageable pageable);
}
