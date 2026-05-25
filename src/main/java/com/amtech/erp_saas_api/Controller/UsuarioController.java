package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.DTO.Request.UsuarioRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.UsuarioResponseDTO;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * POST /api/v1/usuarios
     * Sin token  → onboarding, empresaId viene en el body
     * Con token  → admin crea usuarios, empresaId viene del JWT
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @Valid @RequestBody UsuarioRequestDTO request) {

        Integer empresaId = (userDetails != null)
                ? userDetails.getEmpresaId()
                : request.empresaId();

        if (empresaId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crearUsuario(empresaId, request));
    }

    // PUT /api/v1/usuarios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequestDTO request) {

        return ResponseEntity.ok(
                usuarioService.actualizarUsuario(userDetails.getEmpresaId(), id, request));
    }

    // PATCH /api/v1/usuarios/{id}/desactivar
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id) {

        usuarioService.desactivarUsuario(userDetails.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/usuarios/{id}/activar
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @PathVariable Integer id) {

        usuarioService.activarUsuario(userDetails.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(
            @AuthenticationPrincipal ErpUserDetails userDetails) {

        return ResponseEntity.ok(
                usuarioService.listarPorEmpresa(userDetails.getEmpresaId()));
    }
}