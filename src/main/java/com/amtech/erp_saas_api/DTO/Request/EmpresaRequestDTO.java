package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmpresaRequestDTO(
        @NotBlank(message = "La razón social es obligatoria")
        String razonSocial,

        @NotBlank(message = "El RUC es obligatorio")
        @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos")
        String ruc
) {}
