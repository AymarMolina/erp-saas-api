package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequestDTO(
        @NotBlank(message = "La razón social es obligatoria")
        @Size(max = 150, message = "La razón social no puede exceder los 150 caracteres")
        String razonSocial,

        @NotBlank(message = "El documento de identidad (RUC) es obligatorio")
        @Size(max = 20, message = "El documento no puede exceder los 20 caracteres")
        String documentoIdentidad,

        @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
        String telefono,

        @Email(message = "Debe ser un correo electrónico válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        String email
) {}