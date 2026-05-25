package com.amtech.erp_saas_api.DTO.Response;

public record ClienteResponseDTO(
        Integer id,
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        String direccion
) {}