package com.amtech.erp_saas_api.DTO.Response;

public record ProveedorResponseDTO(
        Integer id,
        String razonSocial,
        String documentoIdentidad,
        String telefono,
        String email,
        Boolean estado
) {}