package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequestDTO(
        @Size(max = 100, message = "El código de barras no puede exceder los 100 caracteres")
        String codigoBarras,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
        String nombre,

        @NotNull(message = "La categoría es obligatoria")
        Integer categoriaId,

        @NotNull(message = "La unidad de medida es obligatoria")
        Integer unidadMedidaId,

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
        BigDecimal precioVenta
) {}