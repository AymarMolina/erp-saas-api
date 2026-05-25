package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Integer id,
        String codigoBarras,
        String nombre,

        Integer categoriaId,
        String categoriaNombre,
        Integer unidadMedidaId,
        String unidadMedidaAbreviatura,

        BigDecimal precioVenta,
        BigDecimal stockFisico,
        BigDecimal stockReservado,
        BigDecimal stockDisponible,
        Boolean estado
) {}