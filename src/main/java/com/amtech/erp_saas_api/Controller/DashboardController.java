package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Response.DashboardResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> obtenerDashboard(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        DashboardResponseDTO dashboardData = dashboardService.obtenerDatosDashboard(userDetails.getEmpresaId());
        return ResponseEntity.ok(dashboardData);
    }
}