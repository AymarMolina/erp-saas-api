package com.amtech.erp_saas_api.Repository;

import com.amtech.erp_saas_api.Entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByIdAndEmpresaId(Integer id, Integer empresaId);

    List<Categoria> findByEmpresaId(Integer empresaId);

    @Query("SELECT c FROM Categoria c WHERE c.empresa.id = :empresaId ORDER BY c.nombre ASC")
    List<Categoria> findCategoriasActivasPorEmpresa(@Param("empresaId") Integer empresaId);

    boolean existsByEmpresaIdAndNombre(Integer empresaId, String nombre);
}