package com.amtech.erp_saas_api.DTO.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EmpresaResponseDTO(
        Integer id,
        String razonSocial,
        String ruc,
        String planSuscripcion,
        Boolean estado,
        LocalDateTime fechaRegistro,
        String logoUrl,
        BigDecimal igvPorcentaje
) {}