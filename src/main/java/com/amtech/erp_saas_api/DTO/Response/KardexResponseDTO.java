package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KardexResponseDTO(
        Long id,
        LocalDateTime fechaMovimiento,
        String tipoMovimiento, // ENTRADA, SALIDA, AJUSTE
        String motivo,
        BigDecimal cantidad,
        BigDecimal saldoLote,

        Integer loteId,
        String codigoLote,
        Integer productoId,
        String nombreProducto
) {}