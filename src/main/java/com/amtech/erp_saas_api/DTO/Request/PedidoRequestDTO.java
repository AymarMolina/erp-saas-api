package com.amtech.erp_saas_api.DTO.Request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequestDTO(

        @NotNull(message = "El cliente es obligatorio")
        Integer clienteId,

        @NotNull(message = "El vendedor es obligatorio")
        Integer vendedorId,

        // El embalador puede asignarse después, es opcional al crear
        Integer embaladorId,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<PedidoDetalleRequestDTO> detalles
) {}