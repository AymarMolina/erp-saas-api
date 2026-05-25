package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {

    @Query("SELECT l FROM Lote l WHERE l.id = :id AND l.producto.empresa.id = :empresaId")
    Optional<Lote> findByIdAndEmpresaId(@Param("id") Integer id, @Param("empresaId") Integer empresaId);

    @Query("""
        SELECT l FROM Lote l 
        WHERE l.producto.id = :productoId 
          AND l.estado = 'ACTIVO' 
        ORDER BY l.fechaVencimiento ASC
        """)
    List<Lote> findLotesActivosPorProductoFEFO(@Param("productoId") Integer productoId);

    @Query("""
        SELECT l FROM Lote l 
        WHERE l.producto.empresa.id = :empresaId 
          AND l.estado = 'ACTIVO' 
          AND l.fechaVencimiento <= :fechaLimite 
        ORDER BY l.fechaVencimiento ASC
        """)
    List<Lote> findLotesProximosAVencer(@Param("empresaId") Integer empresaId, @Param("fechaLimite") LocalDate fechaLimite);

    List<Lote> findByCompraId(Integer compraId);

    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.estado = 'AGOTADO'")
    List<Lote> findLotesAgotadosPorProducto(@Param("productoId") Integer productoId);
}