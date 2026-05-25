package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.ProveedorRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ProveedorResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody ProveedorRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proveedorService.crearProveedor(userDetails.getEmpresaId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequestDTO request) {

        return ResponseEntity.ok(
                proveedorService.actualizarProveedor(userDetails.getEmpresaId(), id, request));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponseDTO>> listarActivos(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        return ResponseEntity.ok(proveedorService.listarActivos(userDetails.getEmpresaId()));
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> buscar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam String razonSocial) {

        return ResponseEntity.ok(
                proveedorService.buscarPorRazonSocial(userDetails.getEmpresaId(), razonSocial));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id) {

        proveedorService.desactivarProveedor(userDetails.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}