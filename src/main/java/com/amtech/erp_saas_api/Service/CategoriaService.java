package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.CategoriaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.CategoriaResponseDTO;
import com.amtech.erp_saas_api.Entity.Categoria;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Repository.CategoriaRepository;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional
    public CategoriaResponseDTO crearCategoria(Integer empresaId, CategoriaRequestDTO request) {
        if (categoriaRepository.existsByEmpresaIdAndNombre(empresaId, request.nombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.nombre());
        }

        Empresa empresaRef = empresaRepository.getReferenceById(empresaId);

        Categoria nueva = Categoria.builder()
                .empresa(empresaRef)
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(true)
                .build();

        return toDTO(categoriaRepository.save(nueva));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarActivasPorEmpresa(Integer empresaId) {
        return categoriaRepository.findCategoriasActivasPorEmpresa(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public CategoriaResponseDTO actualizarCategoria(Integer empresaId, Integer id, CategoriaRequestDTO request) {
        Categoria categoria = buscarEnEmpresa(empresaId, id);

        if (!categoria.getNombre().equalsIgnoreCase(request.nombre()) &&
                categoriaRepository.existsByEmpresaIdAndNombre(empresaId, request.nombre())) {
            throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + request.nombre());
        }

        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());

        return toDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void desactivarCategoria(Integer empresaId, Integer id) {
        Categoria categoria = buscarEnEmpresa(empresaId, id);
        categoria.setEstado(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void activarCategoria(Integer empresaId, Integer id) {
        Categoria categoria = buscarEnEmpresa(empresaId, id);
        categoria.setEstado(true);
        categoriaRepository.save(categoria);
    }

    // ── Helper privado ───────────────────────────────────────────────
    private Categoria buscarEnEmpresa(Integer empresaId, Integer id) {
        return categoriaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada o no pertenece a esta empresa"));
    }

    private CategoriaResponseDTO toDTO(Categoria c) {
        return new CategoriaResponseDTO(
                c.getId(),
                c.getNombre(),
                c.getDescripcion(),
                c.getEstado()
        );
    }
}