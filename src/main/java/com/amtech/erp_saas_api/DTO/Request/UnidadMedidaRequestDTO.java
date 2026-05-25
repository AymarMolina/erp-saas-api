package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadMedidaRequestDTO(

        @NotBlank(message = "El nombre de la unidad es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "La abreviatura es obligatoria")
        @Size(max = 10)
        String abreviatura,

        Boolean permiteFraccion
) {}