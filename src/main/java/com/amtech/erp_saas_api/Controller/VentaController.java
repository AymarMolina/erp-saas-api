package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.VentaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.VentaResponseDTO;
import com.amtech.erp_saas_api.Entity.Venta;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<?> registrarVenta(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody VentaRequestDTO request) {

        try {
            Venta ventaGuardada = ventaService.registrarVenta(
                    userDetails.getEmpresaId(),
                    userDetails.getUsuarioId(),
                    request
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Venta procesada con éxito",
                    "ventaId", ventaGuardada.getId(),
                    "comprobante", ventaGuardada.getComprobante(),
                    "total", ventaGuardada.getTotal()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listar(
            @AuthenticationPrincipal ErpUserDetails userDetails) {
        return ResponseEntity.ok(ventaService.listarVentas(userDetails.getEmpresaId()));
    }
}