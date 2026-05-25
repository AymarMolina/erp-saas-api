package com.amtech.erp_saas_api.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CompraRequestDTO(
        @NotNull(message = "El proveedor es obligatorio")
        Integer proveedorId,

        @NotBlank(message = "El comprobante (factura) es obligatorio")
        String comprobante,

        @NotEmpty(message = "Debe ingresar al menos un producto en la compra")
        @Valid
        List<CompraDetalleRequestDTO> detalles
) {}