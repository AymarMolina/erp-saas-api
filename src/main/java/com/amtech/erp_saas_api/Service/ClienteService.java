package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.ClienteRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ClienteResponseDTO;
import com.amtech.erp_saas_api.Entity.Cliente;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Repository.ClienteRepository;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public ClienteResponseDTO crearCliente(Integer empresaId, ClienteRequestDTO request) {
        if (request.documentoIdentidad() != null && !request.documentoIdentidad().isBlank()) {
            if (clienteRepository.existsByEmpresaIdAndDocumentoIdentidad(empresaId, request.documentoIdentidad())) {
                throw new IllegalArgumentException("Ya existe un cliente con el documento: " + request.documentoIdentidad());
            }
        }

        Empresa empresaRef = empresaRepository.getReferenceById(empresaId);

        Cliente nuevoCliente = Cliente.builder()
                .empresa(empresaRef)
                .nombreCompleto(request.nombreCompleto())
                .documentoIdentidad(request.documentoIdentidad())
                .telefono(request.telefono())
                .email(request.email())
                .direccion(request.direccion())
                .build();

        return mapearAResponse(clienteRepository.save(nuevoCliente));
    }

    @Transactional
    public ClienteResponseDTO actualizarCliente(Integer empresaId, Integer id, ClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado o no pertenece a esta empresa"));

        if (request.documentoIdentidad() != null && !request.documentoIdentidad().isBlank() &&
                !request.documentoIdentidad().equals(cliente.getDocumentoIdentidad())) {

            if (clienteRepository.existsByEmpresaIdAndDocumentoIdentidad(empresaId, request.documentoIdentidad())) {
                throw new IllegalArgumentException("Ya existe otro cliente con el documento: " + request.documentoIdentidad());
            }
        }

        cliente.setNombreCompleto(request.nombreCompleto());
        cliente.setDocumentoIdentidad(request.documentoIdentidad());
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());
        cliente.setDireccion(request.direccion());

        return mapearAResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorDocumento(Integer empresaId, String documento) {
        Cliente cliente = clienteRepository.findByEmpresaIdAndDocumentoIdentidad(empresaId, documento)
                .orElseThrow(() -> new RuntimeException("No se encontró cliente con documento: " + documento));
        return mapearAResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> buscarPorNombre(Integer empresaId, String nombre) {
        return clienteRepository.findByEmpresaIdAndNombreCompletoContainingIgnoreCase(empresaId, nombre)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    private ClienteResponseDTO mapearAResponse(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getDireccion()
        );
    }
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosPorEmpresa(Integer empresaId) {
        return clienteRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }
}