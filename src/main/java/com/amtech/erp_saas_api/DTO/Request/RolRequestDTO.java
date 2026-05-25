package com.amtech.erp_saas_api.DTO.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RolRequestDTO(

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50)
        String nombre,

        @Size(max = 255)
        String descripcion,

        @NotEmpty(message = "Debe asignar al menos un módulo al rol")
        List<Integer> moduloIds
) {}