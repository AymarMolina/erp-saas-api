package com.amtech.erp_saas_api.DTO.Request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record KardexAjusteRequestDTO(
        @NotNull(message = "El ID del lote es obligatorio")
        Integer loteId,

        @NotNull(message = "La cantidad es obligatoria (puede ser negativa si es pérdida)")
        BigDecimal cantidad,

        @NotBlank(message = "Debe justificar el motivo del ajuste (ej: Producto roto, Caducidad, etc.)")
        String motivo
) {}