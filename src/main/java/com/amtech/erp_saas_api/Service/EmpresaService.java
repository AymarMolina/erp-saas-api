package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.EmpresaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.EmpresaResponseDTO;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class   EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Transactional
    public EmpresaResponseDTO registrarEmpresa(EmpresaRequestDTO request) {
        if (empresaRepository.existsByRuc(request.ruc())) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el RUC: " + request.ruc());
        }

        Empresa nuevaEmpresa = Empresa.builder()
                .razonSocial(request.razonSocial())
                .ruc(request.ruc())
                .planSuscripcion("BASICO")
                .estado(true)
                .fechaRegistro(LocalDateTime.now())
                .build();

        Empresa empresaGuardada = empresaRepository.save(nuevaEmpresa);

        return mapearAResponse(empresaGuardada);
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerPorId(Integer id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        return mapearAResponse(empresa);
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarActivas() {
        return empresaRepository.findAllActivas()
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public void suspenderEmpresa(Integer id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresa.setEstado(false);
        empresaRepository.save(empresa);
    }

    private EmpresaResponseDTO mapearAResponse(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getRazonSocial(),
                empresa.getRuc(),
                empresa.getPlanSuscripcion(),
                empresa.getEstado(),
                empresa.getFechaRegistro()
        );
    }
}