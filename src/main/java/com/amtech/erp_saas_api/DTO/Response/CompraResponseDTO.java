package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponseDTO(
        Integer id,
        Integer empresaId,
        Integer proveedorId,
        String proveedorRazonSocial,
        String documentoIdentidad,
        Integer usuarioId,
        String usuarioNombre,
        LocalDateTime fechaCompra,
        String comprobante,
        BigDecimal total,
        String estado,

        String condicionPago,
        String estadoPago,
        LocalDate fechaVencimiento,
        BigDecimal saldoPendiente,

        List<LoteResponseDTO> lotes
) {}