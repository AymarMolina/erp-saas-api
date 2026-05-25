package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.KardexAjusteRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.KardexResponseDTO;
import com.amtech.erp_saas_api.Service.KardexService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    // Ejemplo: GET /api/v1/kardex/producto/10
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<KardexResponseDTO>> historialPorProducto(
            @RequestHeader("X-Empresa-Id") Integer empresaId,
            @PathVariable Integer productoId) {

        return ResponseEntity.ok(kardexService.obtenerHistorialPorProducto(empresaId, productoId));
    }

    // Ejemplo: GET /api/v1/kardex/reporte?inicio=2026-05-01&fin=2026-05-31
    @GetMapping("/reporte")
    public ResponseEntity<List<KardexResponseDTO>> reporteGeneral(
            @RequestHeader("X-Empresa-Id") Integer empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime fechaInicio = inicio.atStartOfDay();
        LocalDateTime fechaFin = fin.atTime(23, 59, 59);

        return ResponseEntity.ok(kardexService.obtenerKardexPorFechas(empresaId, fechaInicio, fechaFin));
    }

    @PostMapping("/ajuste")
    public ResponseEntity<KardexResponseDTO> ajustarInventario(
            @RequestHeader("X-Empresa-Id") Integer empresaId,
            @Valid @RequestBody KardexAjusteRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kardexService.registrarAjusteManual(empresaId, request));
    }
}