package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.VentaRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.VentaDetalleRequestDTO;
import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Venta registrarVenta(Integer empresaId, Integer usuarioId, VentaRequestDTO request) {

        Empresa empresa = empresaRepository.getReferenceById(empresaId);
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        Cliente cliente = null;
        if (request.clienteId() != null) {
            cliente = clienteRepository.findByIdAndEmpresaId(request.clienteId(), empresaId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        }

        Venta venta = Venta.builder()
                .empresa(empresa)
                .cliente(cliente)
                .usuario(usuario)
                .tipoComprobante(request.tipoComprobante())
                .comprobante(generarNumeroComprobante(request.tipoComprobante()))
                .fechaVenta(LocalDateTime.now())
                .estado(Venta.EstadoVenta.COMPLETADA)
                .build();

        List<VentaDetalle> listaDetalles = new ArrayList<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (VentaDetalleRequestDTO item : request.detalles()) {

            Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresaId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.productoId()));

            if (!producto.getUnidadMedida().getPermiteFraccion() && item.cantidad().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                throw new RuntimeException("El producto " + producto.getNombre() + " no permite venta en decimales.");
            }

            if (producto.getStockDisponible().compareTo(item.cantidad()) < 0) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() + ". Disponible: " + producto.getStockDisponible());
            }

            BigDecimal cantidadFaltantePorAsignar = item.cantidad();
            BigDecimal precioOficial = producto.getPrecioVenta();

            List<Lote> lotesDisponibles = loteRepository.findLotesActivosPorProductoFEFO(producto.getId());

            for (Lote lote : lotesDisponibles) {
                if (cantidadFaltantePorAsignar.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal cantidadATomar = lote.getCantidadActual().min(cantidadFaltantePorAsignar);

                BigDecimal subtotalDetalle = cantidadATomar.multiply(precioOficial);

                VentaDetalle detalle = VentaDetalle.builder()
                        .venta(venta)
                        .lote(lote)
                        .cantidad(cantidadATomar)
                        .precioUnitario(precioOficial)
                        .subtotal(subtotalDetalle)
                        .build();

                lote.setCantidadActual(lote.getCantidadActual().subtract(cantidadATomar));
                listaDetalles.add(detalle);
                totalVenta = totalVenta.add(subtotalDetalle);
                cantidadFaltantePorAsignar = cantidadFaltantePorAsignar.subtract(cantidadATomar);
            }
        }

        BigDecimal subtotalSinImpuesto = totalVenta.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal impuestoTotal = totalVenta.subtract(subtotalSinImpuesto);

        venta.setSubtotalSinImpuesto(subtotalSinImpuesto);
        venta.setImpuestoTotal(impuestoTotal);
        venta.setTotal(totalVenta);
        venta.setDetalle(listaDetalles);

        return ventaRepository.save(venta);
    }

    private String generarNumeroComprobante(Venta.TipoComprobante tipo) {
        String prefijo = switch (tipo) {
            case FACTURA -> "F001-";
            case BOLETA -> "B001-";
            default -> "T001-";
        };
        String uuidCorto = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefijo + uuidCorto;
    }
}