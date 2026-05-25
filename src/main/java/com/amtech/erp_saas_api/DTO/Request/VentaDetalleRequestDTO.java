package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VentaDetalleRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad
) {}