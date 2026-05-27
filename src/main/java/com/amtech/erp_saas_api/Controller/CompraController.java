package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.CompraRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.CompraResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<String> registrar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody CompraRequestDTO request) {

        compraService.registrarCompra(userDetails.getEmpresaId(), userDetails.getUsuarioId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Compra registrada con éxito.");
    }

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listar(
            @AuthenticationPrincipal ErpUserDetails userDetails) {
        
        return ResponseEntity.ok(compraService.listarCompras(userDetails.getEmpresaId()));
    }
}