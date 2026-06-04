package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.PerfilRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.UsuarioRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.UsuarioResponseDTO;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Entity.Rol;
import com.amtech.erp_saas_api.Entity.Usuario;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import com.amtech.erp_saas_api.Repository.RolRepository;
import com.amtech.erp_saas_api.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final ArchivoService archivoService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UsuarioResponseDTO crearUsuario(Integer empresaId, UsuarioRequestDTO request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (usuarioRepository.existsByEmpresaIdAndUsername(empresaId, request.username())) {
            throw new IllegalArgumentException("El username '" + request.username() + "' ya está en uso en esta empresa");
        }

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = Usuario.builder()
                .empresa(empresa)
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .nombreCompleto(request.nombreCompleto())
                .rol(rol)
                .estado(true)
                .build();

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Integer empresaId, Integer id, UsuarioRequestDTO request) {
        Usuario usuario = buscarEnEmpresa(empresaId, id);

        Rol rol = rolRepository.findById(request.rolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setRol(rol);

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivarUsuario(Integer empresaId, Integer id) {
        Usuario usuario = buscarEnEmpresa(empresaId, id);
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activarUsuario(Integer empresaId, Integer id) {
        Usuario usuario = buscarEnEmpresa(empresaId, id);
        usuario.setEstado(true);
        usuarioRepository.save(usuario);
    }

    public List<UsuarioResponseDTO> listarPorEmpresa(Integer empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Helper privado ───────────────────────────────────────────────
    private Usuario buscarEnEmpresa(Integer empresaId, Integer usuarioId) {
        return usuarioRepository.findByEmpresaId(empresaId)
                .stream()
                .filter(u -> u.getId().equals(usuarioId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en esta empresa"));
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getEmpresa().getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getRol().getId(),
                u.getRol().getNombre(),
                u.getEstado(),
                u.getFechaCreacion(),
                u.getFotoUrl()
        );
    }
    @Transactional
    public UsuarioResponseDTO actualizarFotoPerfil(Integer usuarioId, Integer empresaId, MultipartFile archivo) {
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFotoUrl() != null && !usuario.getFotoUrl().isBlank()) {
            archivoService.eliminar(usuario.getFotoUrl());
        }
        String url = archivoService.guardar(archivo, "fotos-perfil");
        usuario.setFotoUrl(url);

        return toDTO(usuarioRepository.save(usuario));
    }
    @Transactional
    public UsuarioResponseDTO actualizarMiPerfil(Integer usuarioId, Integer empresaId, PerfilRequestDTO request) {
        // Usamos el repositorio para asegurar que el usuario pertenece a la empresa
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombreCompleto(request.nombreCompleto());

        // Solo si envía un nuevo password, lo hasheamos y guardamos
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toDTO(usuarioRepository.save(usuario));
    }
}