package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.EmpresaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.EmpresaResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> registrarEmpresa(@Valid @RequestBody EmpresaRequestDTO request) {
        EmpresaResponseDTO nuevaEmpresa = empresaService.registrarEmpresa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaEmpresa);
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDTO>> listarEmpresasActivas() {
        List<EmpresaResponseDTO> empresas = empresaService.listarActivas();
        return ResponseEntity.ok(empresas); // Devuelve un 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> obtenerEmpresaPorId(@PathVariable Integer id) {
        EmpresaResponseDTO empresa = empresaService.obtenerPorId(id);
        return ResponseEntity.ok(empresa);
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<Void> suspenderEmpresa(@PathVariable Integer id) {
        empresaService.suspenderEmpresa(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/logo")
    public ResponseEntity<EmpresaResponseDTO> subirLogo(
            @PathVariable Integer id,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal ErpUserDetails userDetails) {
        // Solo el admin de esa empresa puede cambiar su logo
        if (!userDetails.getEmpresaId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(empresaService.actualizarLogo(id, archivo));
    }
    @PatchMapping("/{id}/igv")
    public ResponseEntity<EmpresaResponseDTO> actualizarIgv(
            @PathVariable Integer id,
            @RequestParam("porcentaje") BigDecimal porcentaje,
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        // Solo el admin de esa empresa puede cambiar su IGV
        if (!userDetails.getEmpresaId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(empresaService.actualizarIgv(id, porcentaje));
    }
}