package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
        Integer id,
        Integer empresaId,
        Integer clienteId,
        String clienteNombre,
        Integer usuarioId,
        String usuarioNombre,
        LocalDateTime fechaVenta,
        String comprobante,
        String tipoComprobante,
        BigDecimal subtotalSinImpuesto,
        BigDecimal impuestoTotal,
        BigDecimal total,
        String estado,
        List<VentaDetalleResponseDTO> detalle
) {
    public record VentaDetalleResponseDTO(
            Integer id,
            Integer loteId,
            String productoNombre,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {}
}