package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.Entity.Venta;
import com.amtech.erp_saas_api.Repository.VentaRepository;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource; // ✅ Importación correcta
import org.springframework.http.HttpHeaders;  // ✅ Faltaba
import org.springframework.http.MediaType;    // ✅ Faltaba
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException; // ✅ La correcta (java.io)
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final VentaRepository ventaRepository;
    private final ExcelExportService excelExportService;

    @GetMapping("/ventas/excel")
    public ResponseEntity<Resource> descargarReporteVentas(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {
        
        List<Venta> ventas = ventaRepository.findByEmpresaId(userDetails.getEmpresaId());
        ByteArrayInputStream stream = excelExportService.exportarVentas(ventas);
        
        InputStreamResource file = new InputStreamResource(stream);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}