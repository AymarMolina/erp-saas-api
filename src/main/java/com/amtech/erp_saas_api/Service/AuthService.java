package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.LoginRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.LoginResponseDTO;
import com.amtech.erp_saas_api.Entity.Usuario;
import com.amtech.erp_saas_api.Repository.UsuarioRepository;
import com.amtech.erp_saas_api.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {

        // 1. Buscar usuario por empresa + username (constraint único en BD)
        Usuario usuario = usuarioRepository
                .findByEmpresaIdAndUsername(request.empresaId(), request.username())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // 2. Verificar que la cuenta esté activa
        if (!usuario.getEstado()) {
            throw new RuntimeException("Usuario inactivo. Contacte al administrador.");
        }

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // 4. Generar JWT con datos del usuario
        String token = jwtService.generarToken(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmpresa().getId(),
                usuario.getRol().getNombre()
        );

        return new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getUsername(),
                usuario.getEmpresa().getId(),
                usuario.getRol().getNombre()
        );
    }
}