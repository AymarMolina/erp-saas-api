package com.amtech.erp_saas_api.DTO.Response;

import java.util.List;

public record RolResponseDTO(
        Integer id,
        String nombre,
        String descripcion,
        Boolean estado,
        List<String> modulos
) {}