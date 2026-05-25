package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.UnidadMedidaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.UnidadMedidaResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.UnidadMedidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;

    @PostMapping
    public ResponseEntity<UnidadMedidaResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody UnidadMedidaRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unidadMedidaService.crearUnidad(userDetails.getEmpresaId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedidaResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody UnidadMedidaRequestDTO request) {

        return ResponseEntity.ok(
                unidadMedidaService.actualizarUnidad(userDetails.getEmpresaId(), id, request));
    }

    @GetMapping
    public ResponseEntity<List<UnidadMedidaResponseDTO>> listar(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        return ResponseEntity.ok(unidadMedidaService.listarPorEmpresa(userDetails.getEmpresaId()));
    }
}