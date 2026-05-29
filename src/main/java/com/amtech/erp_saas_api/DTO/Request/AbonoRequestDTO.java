package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AbonoRequestDTO(
        @NotNull(message = "El ID de la venta es obligatorio")
        Integer ventaId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.10", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotBlank(message = "El método de pago es obligatorio")
        String metodoPago, // EFECTIVO, YAPE, TRANSFERENCIA, etc.

        String referencia
) {}