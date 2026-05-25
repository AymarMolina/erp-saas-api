package com.amtech.erp_saas_api.Repository;


import com.amtech.erp_saas_api.Entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    Optional<Modulo> findByNombre(String nombre);

    List<Modulo> findByEstado(Boolean estado);

    @Query("SELECT m FROM Modulo m WHERE m.estado = true ORDER BY m.nombre ASC")
    List<Modulo> findAllActivos();
}