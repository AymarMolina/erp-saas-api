package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.RolRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.RolResponseDTO;
import com.amtech.erp_saas_api.Entity.Modulo;
import com.amtech.erp_saas_api.Entity.Rol;
import com.amtech.erp_saas_api.Repository.ModuloRepository;
import com.amtech.erp_saas_api.Repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;

    @Transactional
    public RolResponseDTO crearRol(RolRequestDTO request) {
        if (rolRepository.findByNombre(request.nombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con el nombre '" + request.nombre() + "'");
        }

        List<Modulo> modulos = moduloRepository.findAllById(request.moduloIds());
        if (modulos.size() != request.moduloIds().size()) {
            throw new RuntimeException("Uno o más módulos indicados no existen");
        }

        Rol rol = Rol.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(true)
                .modulos(modulos)
                .build();

        return toDTO(rolRepository.save(rol));
    }

    @Transactional
    public RolResponseDTO actualizarRol(Integer id, RolRequestDTO request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        List<Modulo> modulos = moduloRepository.findAllById(request.moduloIds());
        if (modulos.size() != request.moduloIds().size()) {
            throw new RuntimeException("Uno o más módulos indicados no existen");
        }

        rol.setNombre(request.nombre());
        rol.setDescripcion(request.descripcion());
        rol.setModulos(modulos);

        return toDTO(rolRepository.save(rol));
    }

    public List<RolResponseDTO> listarActivos() {
        return rolRepository.findByEstado(true)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private RolResponseDTO toDTO(Rol r) {
        List<String> nombreModulos = r.getModulos() != null
                ? r.getModulos().stream().map(Modulo::getNombre).toList()
                : List.of();

        return new RolResponseDTO(
                r.getId(),
                r.getNombre(),
                r.getDescripcion(),
                r.getEstado(),
                nombreModulos
        );
    }
}