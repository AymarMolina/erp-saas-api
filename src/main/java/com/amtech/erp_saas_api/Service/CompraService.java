package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.CompraRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.CompraDetalleRequestDTO;
import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarCompra(Integer empresaId, Integer usuarioId, CompraRequestDTO request) {

        if (compraRepository.existsByEmpresaIdAndProveedorIdAndComprobante(empresaId, request.proveedorId(), request.comprobante())) {
            throw new IllegalArgumentException("Ya existe esta factura registrada para este proveedor.");
        }

        Proveedor proveedor = proveedorRepository.findByIdAndEmpresaId(request.proveedorId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

        // Se mantiene usuarioId desde el token
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Compra compra = Compra.builder()
                .empresa(proveedor.getEmpresa())
                .proveedor(proveedor)
                .usuario(usuario)
                .comprobante(request.comprobante())
                .fechaCompra(LocalDateTime.now())
                .estado(Compra.EstadoCompra.COMPLETADA)
                .total(BigDecimal.ZERO)
                .build();

        List<Lote> listaLotes = new ArrayList<>();
        BigDecimal totalCompra = BigDecimal.ZERO;

        for (CompraDetalleRequestDTO item : request.detalles()) {
            Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresaId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado o no pertenece a la empresa."));

            Lote lote = Lote.builder()
                    .compra(compra)
                    .producto(producto)
                    .codigoLote(item.codigoLote())
                    .fechaFabricacion(java.time.LocalDate.now())
                    .fechaVencimiento(item.fechaVencimiento())
                    .costoUnitario(item.costoUnitario())
                    .cantidadInicial(item.cantidad())
                    .cantidadActual(item.cantidad())
                    .estado(Lote.EstadoLote.ACTIVO)
                    .build();

            listaLotes.add(lote);
            totalCompra = totalCompra.add(item.costoUnitario().multiply(item.cantidad()));
        }

        compra.setLotes(listaLotes);
        compra.setTotal(totalCompra);

        compraRepository.save(compra);
    }
}