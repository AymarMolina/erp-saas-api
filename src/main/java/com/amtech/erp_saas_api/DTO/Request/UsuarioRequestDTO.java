package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        Integer empresaId,

        @NotBlank(message = "El username es obligatorio")
        @Size(max = 50)
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150)
        String nombreCompleto,

        @NotNull(message = "El rol es obligatorio")
        Integer rolId
) {}