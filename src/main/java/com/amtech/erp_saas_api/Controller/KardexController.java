package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.KardexAjusteRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.KardexResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.KardexService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<KardexResponseDTO>> historialPorProducto(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer productoId) {

        return ResponseEntity.ok(kardexService.obtenerHistorialPorProducto(userDetails.getEmpresaId(), productoId));
    }

    @GetMapping("/reporte")
    public ResponseEntity<List<KardexResponseDTO>> reporteGeneral(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime fechaInicio = inicio.atStartOfDay();
        LocalDateTime fechaFin = fin.atTime(23, 59, 59);

        return ResponseEntity.ok(kardexService.obtenerKardexPorFechas(userDetails.getEmpresaId(), fechaInicio, fechaFin));
    }

    @PostMapping("/ajuste")
    public ResponseEntity<KardexResponseDTO> ajustarInventario(
            @AuthenticationPrincipal ErpUserDetails userDetails, // 🔥 Cambiado a UserDetails
            @Valid @RequestBody KardexAjusteRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kardexService.registrarAjusteManual(userDetails.getEmpresaId(), request));
    }
}