package com.amtech.erp_saas_api.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SunatResponseDTO(
    @JsonProperty("razon_social") String razonSocial,
    @JsonProperty("numero_documento") String numeroDocumento,
    @JsonProperty("estado") String estado,
    @JsonProperty("condicion") String condicion,
    @JsonProperty("direccion") String direccion
) {}
