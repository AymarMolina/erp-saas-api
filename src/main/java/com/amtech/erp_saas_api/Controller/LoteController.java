package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Response.LoteResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    public ResponseEntity<List<LoteResponseDTO>> listarLotesDisponibles(
            @AuthenticationPrincipal ErpUserDetails userDetails) {
        
        return ResponseEntity.ok(loteService.obtenerLotesActivos(userDetails.getEmpresaId()));
    }
}