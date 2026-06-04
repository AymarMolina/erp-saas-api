package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;

public record LoginResponseDTO(
        String token,
        Integer usuarioId,
        String nombreCompleto,
        String username,
        Integer empresaId,
        String rolNombre,
        BigDecimal igvPorcentaje
) {}