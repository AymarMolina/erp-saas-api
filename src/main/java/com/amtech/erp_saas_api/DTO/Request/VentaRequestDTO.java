package com.amtech.erp_saas_api.DTO.Request;

import com.amtech.erp_saas_api.Entity.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequestDTO(

        Integer clienteId,

        @NotNull(message = "El tipo de comprobante es obligatorio")
        Venta.TipoComprobante tipoComprobante,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Valid
        List<VentaDetalleRequestDTO> detalles
) {}