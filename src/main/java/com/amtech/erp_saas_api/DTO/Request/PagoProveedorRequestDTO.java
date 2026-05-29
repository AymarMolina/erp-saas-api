package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PagoProveedorRequestDTO(
        @NotNull(message = "El ID de la compra es obligatorio") Integer compraId,
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") BigDecimal monto,
        @NotNull(message = "El método de pago es obligatorio") String metodoPago,
        String referencia
) {}