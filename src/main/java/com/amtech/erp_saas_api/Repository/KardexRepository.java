package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {

    @Query("""
        SELECT k FROM Kardex k 
        WHERE k.lote.id = :loteId 
          AND k.lote.producto.empresa.id = :empresaId 
        ORDER BY k.fechaMovimiento DESC
        """)
    List<Kardex> findKardexPorLote(@Param("loteId") Integer loteId, @Param("empresaId") Integer empresaId);

    @Query("""
        SELECT k FROM Kardex k 
        WHERE k.lote.producto.id = :productoId 
          AND k.lote.producto.empresa.id = :empresaId 
        ORDER BY k.fechaMovimiento DESC
        """)
    List<Kardex> findKardexPorProducto(@Param("productoId") Integer productoId, @Param("empresaId") Integer empresaId);


    @Query("""
        SELECT k FROM Kardex k 
        WHERE k.lote.producto.empresa.id = :empresaId 
          AND k.tipoMovimiento = :tipo 
        ORDER BY k.fechaMovimiento DESC
        """)
    List<Kardex> findKardexPorTipoMovimiento(
            @Param("empresaId") Integer empresaId,
            @Param("tipo") Kardex.TipoMovimiento tipo
    );

    // 1. La consulta GLOBAL (Corregida)
    @Query("SELECT k FROM Kardex k WHERE k.lote.producto.empresa.id = :empresaId AND k.fechaMovimiento BETWEEN :inicio AND :fin")
    List<Kardex> findKardexPorRangoFechas(
            @Param("empresaId") Integer empresaId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // 2. La consulta ESPECÍFICA POR PRODUCTO
    @Query("SELECT k FROM Kardex k LEFT JOIN k.lote l LEFT JOIN l.producto p WHERE p.empresa.id = :empresaId AND p.id = :productoId AND k.fechaMovimiento BETWEEN :inicio AND :fin")
    List<Kardex> findKardexPorRangoFechasYProducto(
            @Param("empresaId") Integer empresaId,
            @Param("productoId") Integer productoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}