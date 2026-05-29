package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.CompraRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.CompraDetalleRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.PagoProveedorRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.CompraResponseDTO;
import com.amtech.erp_saas_api.DTO.Response.LoteResponseDTO;
import com.amtech.erp_saas_api.DTO.Response.PagoProveedorResponseDTO;
import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final PagoProveedorRepository pagoProveedorRepository; // <-- Agregar esta inyección

    @Transactional
    public void registrarCompra(Integer empresaId, Integer usuarioId, CompraRequestDTO request) {

        if (compraRepository.existsByEmpresaIdAndProveedorIdAndComprobante(empresaId, request.proveedorId(), request.comprobante())) {
            throw new IllegalArgumentException("Ya existe esta factura registrada para este proveedor.");
        }

        Proveedor proveedor = proveedorRepository.findByIdAndEmpresaId(request.proveedorId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado."));

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
                    .fechaFabricacion(LocalDate.now())
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

        // --- LÓGICA DE CRÉDITOS ---
        String condicion = request.condicionPago() != null ? request.condicionPago().toUpperCase() : "CONTADO";
        BigDecimal pagoInicial = request.pagoInicial() != null ? request.pagoInicial() : BigDecimal.ZERO;

        if (condicion.equals("CREDITO")) {
            compra.setCondicionPago(Compra.CondicionPago.CREDITO);
            BigDecimal saldo = totalCompra.subtract(pagoInicial);
            compra.setSaldoPendiente(saldo);

            if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
                compra.setEstadoPago(Compra.EstadoPago.PAGADO);
            } else if (pagoInicial.compareTo(BigDecimal.ZERO) > 0) {
                compra.setEstadoPago(Compra.EstadoPago.PARCIAL);
            } else {
                compra.setEstadoPago(Compra.EstadoPago.PENDIENTE);
            }

            if (request.diasCredito() != null) {
                compra.setFechaVencimiento(LocalDate.now().plusDays(request.diasCredito()));
            }
        } else {
            compra.setCondicionPago(Compra.CondicionPago.CONTADO);
            compra.setEstadoPago(Compra.EstadoPago.PAGADO);
            compra.setSaldoPendiente(BigDecimal.ZERO);
        }

        Compra compraGuardada = compraRepository.save(compra);

        // Si es crédito y se dejó un pago inicial, lo registramos en la tabla de pagos
        if (condicion.equals("CREDITO") && pagoInicial.compareTo(BigDecimal.ZERO) > 0) {
            PagoProveedor pago = PagoProveedor.builder()
                    .compra(compraGuardada)
                    .usuario(usuario)
                    .monto(pagoInicial)
                    .metodoPago(request.metodoPagoInicial() != null ? request.metodoPagoInicial() : "EFECTIVO")
                    .referencia("Adelanto inicial de compra")
                    .fechaPago(LocalDateTime.now())
                    .build();
            pagoProveedorRepository.save(pago);
        }
    }

    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarCompras(Integer empresaId) {
        return compraRepository.findByEmpresaIdOrderByFechaCompraDesc(empresaId)
                .stream()
                .map(this::mapearA_DTO)
                .toList();
    }

    // --- NUEVO: Listar compras pendientes por pagar ---
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> listarComprasPendientes(Integer empresaId) {
        return compraRepository.findComprasConDeudaPendiente(empresaId)
                .stream()
                .map(this::mapearA_DTO)
                .toList();
    }

    // --- NUEVO: Registrar un abono a un proveedor ---
    @Transactional
    public PagoProveedorResponseDTO registrarPago(Integer empresaId, Integer usuarioId, PagoProveedorRequestDTO request) {
        Compra compra = compraRepository.findByIdAndEmpresaId(request.compraId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));

        if (compra.getSaldoPendiente().compareTo(request.monto()) < 0) {
            throw new RuntimeException("El monto (S/ " + request.monto() + ") supera la deuda actual (S/ " + compra.getSaldoPendiente() + ")");
        }

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        PagoProveedor pago = PagoProveedor.builder()
                .compra(compra)
                .usuario(usuario)
                .monto(request.monto())
                .metodoPago(request.metodoPago())
                .referencia(request.referencia())
                .fechaPago(LocalDateTime.now())
                .build();

        PagoProveedor guardado = pagoProveedorRepository.save(pago);

        return new PagoProveedorResponseDTO(
                guardado.getId(),
                guardado.getCompra().getId(),
                guardado.getUsuario().getNombreCompleto(),
                guardado.getFechaPago(),
                guardado.getMonto(),
                guardado.getMetodoPago(),
                guardado.getReferencia()
        );
    }

    // --- Helper para mapear ---
    private CompraResponseDTO mapearA_DTO(Compra c) {
        List<LoteResponseDTO> lotesDTO = c.getLotes().stream()
                .map(l -> new LoteResponseDTO(
                        l.getId(), l.getProducto().getId(), l.getProducto().getNombre(), c.getId(),
                        l.getCodigoLote(), l.getFechaFabricacion(), l.getFechaVencimiento(),
                        l.getCostoUnitario(), l.getCantidadInicial(), l.getCantidadActual(), l.getEstado().name()
                )).toList();

        return new CompraResponseDTO(
                c.getId(), c.getEmpresa().getId(), c.getProveedor().getId(), c.getProveedor().getRazonSocial(),
                c.getUsuario().getId(), c.getUsuario().getUsername(), c.getFechaCompra(), c.getComprobante(),
                c.getTotal(), c.getEstado().name(),
                c.getCondicionPago() != null ? c.getCondicionPago().name() : "CONTADO",
                c.getEstadoPago() != null ? c.getEstadoPago().name() : "PAGADO",
                c.getFechaVencimiento(),
                c.getSaldoPendiente() != null ? c.getSaldoPendiente() : BigDecimal.ZERO,
                lotesDTO
        );
    }
}