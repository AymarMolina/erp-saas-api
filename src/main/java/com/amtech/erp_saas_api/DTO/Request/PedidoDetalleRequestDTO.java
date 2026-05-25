package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PedidoDetalleRequestDTO(

        @NotNull(message = "El producto es obligatorio")
        Integer productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
        BigDecimal cantidad,

        @NotNull(message = "El precio acordado es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal precioAcordado
) {}