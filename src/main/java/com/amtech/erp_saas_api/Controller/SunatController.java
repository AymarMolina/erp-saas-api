package com.amtech.erp_saas_api.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amtech.erp_saas_api.DTO.Response.SunatResponseDTO;
import com.amtech.erp_saas_api.Service.SunatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sunat")
@RequiredArgsConstructor
public class SunatController {

    private final SunatService sunatService;

    @GetMapping("/ruc/{numero}")
    public ResponseEntity<SunatResponseDTO> consultar(@PathVariable String numero) {
        return ResponseEntity.ok(sunatService.consultarRuc(numero));
    }
}