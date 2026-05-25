package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CompraResponseDTO(
        Integer id,
        Integer empresaId,
        Integer proveedorId,
        String proveedorRazonSocial,
        Integer usuarioId,
        String usuarioNombre,
        LocalDateTime fechaCompra,
        String comprobante,
        BigDecimal total,
        String estado,
        List<LoteResponseDTO> lotes
) {}