package com.amtech.erp_saas_api.DTO.Response;

public record CategoriaResponseDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean estado
) {}