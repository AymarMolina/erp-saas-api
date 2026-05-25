package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.CategoriaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.CategoriaResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody CategoriaRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.crearCategoria(userDetails.getEmpresaId(), request));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarActivas(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        return ResponseEntity.ok(
                categoriaService.listarActivasPorEmpresa(userDetails.getEmpresaId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequestDTO request) {

        return ResponseEntity.ok(
                categoriaService.actualizarCategoria(userDetails.getEmpresaId(), id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id) {

        categoriaService.desactivarCategoria(userDetails.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id) {

        categoriaService.activarCategoria(userDetails.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}