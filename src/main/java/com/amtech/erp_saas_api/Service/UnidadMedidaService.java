package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.UnidadMedidaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.UnidadMedidaResponseDTO;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Entity.UnidadMedida;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import com.amtech.erp_saas_api.Repository.UnidadMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public UnidadMedidaResponseDTO crearUnidad(Integer empresaId, UnidadMedidaRequestDTO request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        UnidadMedida unidad = UnidadMedida.builder()
                .empresa(empresa)
                .nombre(request.nombre())
                .abreviatura(request.abreviatura())
                .permiteFraccion(request.permiteFraccion() != null ? request.permiteFraccion() : false)
                .build();

        return toDTO(unidadMedidaRepository.save(unidad));
    }

    @Transactional
    public UnidadMedidaResponseDTO actualizarUnidad(Integer empresaId, Integer id, UnidadMedidaRequestDTO request) {
        UnidadMedida unidad = unidadMedidaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Unidad de medida no encontrada"));

        unidad.setNombre(request.nombre());
        unidad.setAbreviatura(request.abreviatura());
        if (request.permiteFraccion() != null) {
            unidad.setPermiteFraccion(request.permiteFraccion());
        }

        return toDTO(unidadMedidaRepository.save(unidad));
    }

    @Transactional(readOnly = true)
    public List<UnidadMedidaResponseDTO> listarPorEmpresa(Integer empresaId) {
        return unidadMedidaRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private UnidadMedidaResponseDTO toDTO(UnidadMedida u) {
        return new UnidadMedidaResponseDTO(
                u.getId(),
                u.getEmpresa().getId(),
                u.getNombre(),
                u.getAbreviatura(),
                u.getPermiteFraccion()
        );
    }
}