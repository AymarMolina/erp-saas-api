package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoProveedorResponseDTO(
        Integer id,
        Integer compraId,
        String usuarioNombre,
        LocalDateTime fechaPago,
        BigDecimal monto,
        String metodoPago,
        String referencia
) {}