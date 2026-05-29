package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AbonoResponseDTO(
        Integer id,
        Integer ventaId,
        String cajeroNombre,
        LocalDateTime fechaPago,
        BigDecimal monto,
        String metodoPago,
        String referencia
) {}