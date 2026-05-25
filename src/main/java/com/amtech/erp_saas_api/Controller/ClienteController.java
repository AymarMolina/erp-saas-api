package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.ClienteRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ClienteResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody ClienteRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.crearCliente(userDetails.getEmpresaId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequestDTO request) {

        return ResponseEntity.ok(
                clienteService.actualizarCliente(userDetails.getEmpresaId(), id, request));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> buscar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) String nombre) {

        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(clienteService.buscarPorNombre(userDetails.getEmpresaId(), nombre));
        }

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteResponseDTO> buscarPorDocumento(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable String documento) {

        return ResponseEntity.ok(
                clienteService.buscarPorDocumento(userDetails.getEmpresaId(), documento));
    }
}