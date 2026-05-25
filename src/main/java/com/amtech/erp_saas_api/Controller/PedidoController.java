package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.PedidoRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.PedidoResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody PedidoRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crearPedido(userDetails.getEmpresaId(), request));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) String estado) {

        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(pedidoService.listarPorEstado(userDetails.getEmpresaId(), estado));
        }
        return ResponseEntity.ok(pedidoService.listarPorEmpresa(userDetails.getEmpresaId()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam String estado) {

        return ResponseEntity.ok(pedidoService.actualizarEstado(userDetails.getEmpresaId(), id, estado));
    }

    @PatchMapping("/{id}/embalador")
    public ResponseEntity<PedidoResponseDTO> asignarEmbalador(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam Integer embaladorId) {

        return ResponseEntity.ok(pedidoService.asignarEmbalador(userDetails.getEmpresaId(), id, embaladorId));
    }
}