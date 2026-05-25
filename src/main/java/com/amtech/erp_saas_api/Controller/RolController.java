package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.RolRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.RolResponseDTO;
import com.amtech.erp_saas_api.Service.RolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    // POST /api/v1/roles  (uso: superadmin del SaaS)
    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(
            @Valid @RequestBody RolRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crearRol(request));
    }

    // PUT /api/v1/roles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody RolRequestDTO request) {

        return ResponseEntity.ok(rolService.actualizarRol(id, request));
    }

    // GET /api/v1/roles
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listarActivos() {
        return ResponseEntity.ok(rolService.listarActivos());
    }
}