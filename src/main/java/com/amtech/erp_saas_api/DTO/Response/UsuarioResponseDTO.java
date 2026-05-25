package com.amtech.erp_saas_api.DTO.Response;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Integer id,
        Integer empresaId,
        String username,
        String nombreCompleto,
        Integer rolId,
        String rolNombre,
        Boolean estado,
        LocalDateTime fechaCreacion
) {}