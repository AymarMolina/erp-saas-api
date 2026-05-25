package com.amtech.erp_saas_api.DTO.Response;

public record UnidadMedidaResponseDTO(
        Integer id,
        Integer empresaId,
        String nombre,
        String abreviatura,
        Boolean permiteFraccion
) {}