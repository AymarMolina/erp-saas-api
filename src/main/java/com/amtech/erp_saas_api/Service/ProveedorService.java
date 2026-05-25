package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.ProveedorRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ProveedorResponseDTO;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Entity.Proveedor;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import com.amtech.erp_saas_api.Repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public ProveedorResponseDTO crearProveedor(Integer empresaId, ProveedorRequestDTO request) {

        if (proveedorRepository.existsByEmpresaIdAndDocumentoIdentidad(empresaId, request.documentoIdentidad())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el RUC/Documento: " + request.documentoIdentidad());
        }

        Empresa empresaRef = empresaRepository.getReferenceById(empresaId);

        Proveedor nuevoProveedor = Proveedor.builder()
                .empresa(empresaRef)
                .razonSocial(request.razonSocial())
                .documentoIdentidad(request.documentoIdentidad())
                .telefono(request.telefono())
                .email(request.email())
                .estado(true)
                .build();

        Proveedor guardado = proveedorRepository.save(nuevoProveedor);
        return mapearAResponse(guardado);
    }

    @Transactional
    public ProveedorResponseDTO actualizarProveedor(Integer empresaId, Integer id, ProveedorRequestDTO request) {
        Proveedor proveedor = proveedorRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado o no pertenece a esta empresa"));

        if (!proveedor.getDocumentoIdentidad().equals(request.documentoIdentidad()) &&
                proveedorRepository.existsByEmpresaIdAndDocumentoIdentidad(empresaId, request.documentoIdentidad())) {
            throw new IllegalArgumentException("Ya existe otro proveedor con el RUC/Documento: " + request.documentoIdentidad());
        }

        proveedor.setRazonSocial(request.razonSocial());
        proveedor.setDocumentoIdentidad(request.documentoIdentidad());
        proveedor.setTelefono(request.telefono());
        proveedor.setEmail(request.email());

        Proveedor actualizado = proveedorRepository.save(proveedor);
        return mapearAResponse(actualizado);
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarActivos(Integer empresaId) {
        return proveedorRepository.findByEmpresaIdAndEstadoTrueOrderByRazonSocialAsc(empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> buscarPorRazonSocial(Integer empresaId, String razonSocial) {
        return proveedorRepository.findByEmpresaIdAndRazonSocialContainingIgnoreCaseAndEstadoTrue(empresaId, razonSocial)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public void desactivarProveedor(Integer empresaId, Integer id) {
        Proveedor proveedor = proveedorRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado o no pertenece a esta empresa"));

        proveedor.setEstado(false);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponseDTO mapearAResponse(Proveedor proveedor) {
        return new ProveedorResponseDTO(
                proveedor.getId(),
                proveedor.getRazonSocial(),
                proveedor.getDocumentoIdentidad(),
                proveedor.getTelefono(),
                proveedor.getEmail(),
                proveedor.getEstado()
        );
    }
}