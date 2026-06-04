package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.EmpresaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.EmpresaResponseDTO;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class   EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ArchivoService archivoService;

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
                empresa.getFechaRegistro(),
                empresa.getLogoUrl(),
                empresa.getIgvPorcentaje()
        );
    }
    @Transactional
    public EmpresaResponseDTO actualizarLogo(Integer empresaId, MultipartFile archivo) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + empresaId));

        if (empresa.getLogoUrl() != null && !empresa.getLogoUrl().isBlank()) {
            archivoService.eliminar(empresa.getLogoUrl());
        }

        String url = archivoService.guardar(archivo, "logos");

        empresa.setLogoUrl(url);
        empresa.setLogoNombreArchivo(archivo.getOriginalFilename());
        empresa.setLogoFechaActualizacion(LocalDateTime.now());

        return mapearAResponse(empresaRepository.save(empresa));
    }
    @Transactional
    public EmpresaResponseDTO actualizarIgv(Integer empresaId, BigDecimal porcentaje) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Validación básica: IGV entre 0 y 100
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje de IGV debe estar entre 0 y 100");
        }

        empresa.setIgvPorcentaje(porcentaje);
        return mapearAResponse(empresaRepository.save(empresa));
    }
}