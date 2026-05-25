package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraDetalleRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotBlank(message = "El código de lote es obligatorio")
        String codigoLote,

        @NotNull(message = "La cantidad inicial es obligatoria")
        BigDecimal cantidad,

        @NotNull(message = "El costo unitario es obligatorio")
        BigDecimal costoUnitario,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento
) {}