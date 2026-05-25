package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.LoginRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.LoginResponseDTO;
import com.amtech.erp_saas_api.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     *
     * Body:
     * {
     *   "empresaId": 1,
     *   "username": "cajero01",
     *   "password": "secreto123"
     * }
     *
     * Response 200:
     * {
     *   "token": "eyJhbGci...",
     *   "usuarioId": 5,
     *   "nombreCompleto": "Juan Pérez",
     *   "username": "cajero01",
     *   "empresaId": 1,
     *   "rolNombre": "CAJERO"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}