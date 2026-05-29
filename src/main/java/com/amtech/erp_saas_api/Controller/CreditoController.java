package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.AbonoRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.AbonoResponseDTO;
import com.amtech.erp_saas_api.DTO.Response.VentaResponseDTO;
import com.amtech.erp_saas_api.Entity.AbonoVenta;
import com.amtech.erp_saas_api.Entity.Venta;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @GetMapping("/pendientes")
    public ResponseEntity<List<VentaResponseDTO>> listarDeudas(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        List<VentaResponseDTO> ventasPendientes = creditoService.obtenerVentasConDeuda(userDetails.getEmpresaId());
        return ResponseEntity.ok(ventasPendientes);
    }

    @PostMapping("/abonos")
    public ResponseEntity<AbonoResponseDTO> registrarAbono(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody AbonoRequestDTO request) {

        AbonoResponseDTO nuevoAbono = creditoService.registrarAbono(
                userDetails.getEmpresaId(),
                userDetails.getUsuarioId(),
                request
        );
        return ResponseEntity.ok(nuevoAbono);
    }

    // Ver todos los pagos que se le han hecho a una venta
    @GetMapping("/ventas/{ventaId}/abonos")
    public ResponseEntity<List<AbonoResponseDTO>> historialAbonos(
            @PathVariable Integer ventaId) {

        List<AbonoResponseDTO> historial = creditoService.obtenerHistorialAbonos(ventaId);
        return ResponseEntity.ok(historial);
    }
}