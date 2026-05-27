package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.ProductoRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.ProductoResponseDTO;
import com.amtech.erp_saas_api.Entity.Categoria;
import com.amtech.erp_saas_api.Entity.Empresa;
import com.amtech.erp_saas_api.Entity.Producto;
import com.amtech.erp_saas_api.Entity.UnidadMedida;
import com.amtech.erp_saas_api.Repository.CategoriaRepository;
import com.amtech.erp_saas_api.Repository.EmpresaRepository;
import com.amtech.erp_saas_api.Repository.ProductoRepository;
import com.amtech.erp_saas_api.Repository.UnidadMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadMedidaRepository unidadMedidaRepository;

    @Transactional
    public ProductoResponseDTO crearProducto(Integer empresaId, ProductoRequestDTO request) {

        if (request.codigoBarras() != null && !request.codigoBarras().isBlank()) {
            if (productoRepository.existsByEmpresaIdAndCodigoBarras(empresaId, request.codigoBarras())) {
                throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + request.codigoBarras());
            }
        }

        Categoria categoria = categoriaRepository.findByIdAndEmpresaId(request.categoriaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Categoría inválida o no autorizada"));

        UnidadMedida unidadMedida = unidadMedidaRepository.findByIdAndEmpresaId(request.unidadMedidaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Unidad de Medida inválida o no autorizada"));

        Empresa empresaRef = empresaRepository.getReferenceById(empresaId);

        Producto nuevoProducto = Producto.builder()
                .empresa(empresaRef)
                .codigoBarras(request.codigoBarras())
                .nombre(request.nombre())
                .categoria(categoria)
                .unidadMedida(unidadMedida)
                .precioVenta(request.precioVenta())
                .estado(true)
                .build();

        Producto guardado = productoRepository.save(nuevoProducto);
        return mapearAResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarTodos(Integer empresaId) {
        return productoRepository.findByEmpresaIdAndEstadoTrue(empresaId)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorCodigoBarrasParaVenta(Integer empresaId, String codigoBarras) {
        Producto producto = productoRepository.findByEmpresaIdAndCodigoBarrasAndEstadoTrue(empresaId, codigoBarras)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo: " + codigoBarras));

        return mapearAResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(Integer empresaId, String nombre) {
        return productoRepository.findByEmpresaIdAndNombreContainingIgnoreCaseAndEstadoTrue(empresaId, nombre)
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }
    // Agrégalo debajo de tu método crearProducto en ProductoService.java

    // Agrégalo debajo de tu método crearProducto en ProductoService.java

    @Transactional
    public ProductoResponseDTO actualizarProducto(Integer empresaId, Integer id, ProductoRequestDTO request) {
        
        // 1. Buscamos el producto
        Producto producto = productoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado o inactivo"));

        // 2. Validamos que el nuevo código de barras no exista ya en OTRO producto de la misma empresa
        if (request.codigoBarras() != null && !request.codigoBarras().isBlank() 
            && !request.codigoBarras().equals(producto.getCodigoBarras())) {
            
            if (productoRepository.existsByEmpresaIdAndCodigoBarras(empresaId, request.codigoBarras())) {
                throw new IllegalArgumentException("Ya existe otro producto con el código de barras: " + request.codigoBarras());
            }
        }

        // 3. Validamos que las nuevas relaciones existan
        Categoria categoria = categoriaRepository.findByIdAndEmpresaId(request.categoriaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Categoría inválida o no autorizada"));

        UnidadMedida unidadMedida = unidadMedidaRepository.findByIdAndEmpresaId(request.unidadMedidaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Unidad de Medida inválida o no autorizada"));

        // 4. Actualizamos los datos
        producto.setCodigoBarras(request.codigoBarras());
        producto.setNombre(request.nombre());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(unidadMedida);
        producto.setPrecioVenta(request.precioVenta());

        // 5. Guardamos y retornamos
        return mapearAResponse(productoRepository.save(producto));
    }
    private ProductoResponseDTO mapearAResponse(Producto p) {
        return new ProductoResponseDTO(
                p.getId(),
                p.getCodigoBarras(),
                p.getNombre(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getUnidadMedida().getId(),
                p.getUnidadMedida().getAbreviatura(),
                p.getPrecioVenta(),
                p.getStockFisico(),
                p.getStockReservado(),
                p.getStockDisponible(),
                p.getEstado()
        );
    }
}