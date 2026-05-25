package com.amtech.erp_saas_api.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Integer id,
        Integer empresaId,
        Integer clienteId,
        String clienteNombre,
        Integer vendedorId,
        String vendedorNombre,
        Integer embaladorId,
        String embaladorNombre,
        LocalDateTime fechaPedido,
        String estado,
        List<DetalleDTO> detalle
) {
    public record DetalleDTO(
            Integer id,
            Integer productoId,
            String productoNombre,
            BigDecimal cantidad,
            BigDecimal precioAcordado,
            BigDecimal subtotal
    ) {}
}