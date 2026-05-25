package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResponseDTO(
        Integer id,
        Integer productoId,
        String productoNombre,
        Integer compraId,
        String codigoLote,
        LocalDate fechaFabricacion,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitario,
        BigDecimal cantidadInicial,
        BigDecimal cantidadActual,
        String estado
) {}