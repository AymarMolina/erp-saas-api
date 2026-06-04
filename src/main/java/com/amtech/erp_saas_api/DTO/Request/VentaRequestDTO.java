package com.amtech.erp_saas_api.DTO.Request;

import com.amtech.erp_saas_api.Entity.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VentaRequestDTO(
        Integer clienteId,

        @NotNull(message = "El tipo de comprobante es obligatorio")
        Venta.TipoComprobante tipoComprobante,

        Venta.CondicionPago condicionPago,
        Integer diasCredito,
        BigDecimal pagoInicial,
        String metodoPagoInicial,

        @NotNull(message = "El porcentaje de IGV es obligatorio")
        BigDecimal igvPorcentaje,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Valid
        List<VentaDetalleRequestDTO> detalles
) {}