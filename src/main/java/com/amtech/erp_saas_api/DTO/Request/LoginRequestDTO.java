package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(

        @NotNull(message = "El ID de empresa es obligatorio")
        Integer empresaId,

        @NotBlank(message = "El username es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}