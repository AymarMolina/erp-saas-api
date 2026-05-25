package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String nombreCompleto,

        @Size(max = 20, message = "El documento no puede exceder los 20 caracteres")
        String documentoIdentidad,

        @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
        String telefono,

        @Email(message = "Debe ser un correo electrónico válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        String email,

        @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
        String direccion
) {}