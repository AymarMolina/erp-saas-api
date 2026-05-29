package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.VentaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.VentaResponseDTO;
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
    private final AbonoVentaRepository abonoVentaRepository;

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

        // =========================================================================
        // ↓ NUEVA LÓGICA DE CRÉDITOS Y CONDICIONES DE PAGO ↓
        // =========================================================================
        Venta.CondicionPago condicion = request.condicionPago() != null ? request.condicionPago() : Venta.CondicionPago.CONTADO;
        venta.setCondicionPago(condicion);

        if (condicion == Venta.CondicionPago.CREDITO) {
            if (cliente == null) {
                throw new RuntimeException("Las ventas a crédito requieren un cliente registrado obligatoriamente.");
            }

            // 1. Calcular deuda actual del cliente
            BigDecimal deudaActualDelCliente = ventaRepository.findByEmpresaIdAndClienteIdOrderByFechaVentaDesc(empresaId, cliente.getId())
                    .stream()
                    .map(Venta::getSaldoPendiente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2. Proyectar nueva deuda sumando esta venta
            BigDecimal proximaDeuda = deudaActualDelCliente.add(totalVenta);

            // 3. Validar límite (Si el límite es mayor a 0, se valida)
            if (cliente.getLimiteCredito() != null &&
                    cliente.getLimiteCredito().compareTo(BigDecimal.ZERO) > 0 &&
                    proximaDeuda.compareTo(cliente.getLimiteCredito()) > 0) {
                throw new RuntimeException("Crédito rechazado. El cliente superaría su límite de S/ " + cliente.getLimiteCredito());
            }

            // 4. Configurar la venta a crédito
            venta.setSaldoPendiente(totalVenta); // Inicia debiendo todo
            venta.setEstadoPago(Venta.EstadoPago.PENDIENTE);
            int dias = request.diasCredito() != null ? request.diasCredito() : 0;
            venta.setFechaVencimiento(java.time.LocalDate.now().plusDays(dias));

        } else {
            // Venta al contado normal
            venta.setSaldoPendiente(BigDecimal.ZERO);
            venta.setEstadoPago(Venta.EstadoPago.PAGADO);
        }
        // =========================================================================

        // Guardamos la Venta
        Venta ventaGuardada = ventaRepository.save(venta);

        // =========================================================================
        // ↓ REGISTRAR PAGO INICIAL / ADELANTO (SI ES A CRÉDITO Y DEJÓ DINERO) ↓
        // =========================================================================
        if (condicion == Venta.CondicionPago.CREDITO && request.pagoInicial() != null && request.pagoInicial().compareTo(BigDecimal.ZERO) > 0) {

            if (request.pagoInicial().compareTo(totalVenta) > 0) {
                throw new RuntimeException("El adelanto no puede ser mayor al total de la venta.");
            }

            AbonoVenta abono = AbonoVenta.builder()
                    .venta(ventaGuardada)
                    .usuario(usuario)
                    .monto(request.pagoInicial())
                    .metodoPago(request.metodoPagoInicial() != null ? request.metodoPagoInicial() : "EFECTIVO")
                    .referencia("Adelanto inicial en POS")
                    .build();

            // Nota: Asegúrate de tener declarado: private final AbonoVentaRepository abonoVentaRepository;
            abonoVentaRepository.save(abono);

            // ¡Magia!: El trigger de la Base de Datos actualizará el 'saldo_pendiente' automáticamente.
        }
        // =========================================================================

        return ventaGuardada;
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

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> listarVentas(Integer empresaId) {
        return ventaRepository.findByEmpresaIdOrderByFechaVentaDesc(empresaId)
                .stream()
                .map(v -> {
                    // Mapeamos los detalles de la venta
                    List<VentaResponseDTO.VentaDetalleResponseDTO> detallesDTO = v.getDetalle().stream()
                            .map(d -> new VentaResponseDTO.VentaDetalleResponseDTO(
                                    d.getId(),
                                    d.getLote() != null ? d.getLote().getId() : null,
                                    d.getLote().getProducto().getNombre(),
                                    d.getCantidad(),
                                    d.getPrecioUnitario(),
                                    d.getSubtotal()
                            ))
                            .toList();

                    // Construimos el DTO principal de manera segura (evitando NullPointerException si no hay cliente)
                    return new VentaResponseDTO(
                            v.getId(),
                            v.getEmpresa().getId(),
                            v.getCliente() != null ? v.getCliente().getId() : null,
                            v.getCliente() != null ? v.getCliente().getNombreCompleto() : null,
                            v.getCliente() !=null ? v.getCliente().getDocumentoIdentidad():null,
                            v.getUsuario().getId(),
                            v.getUsuario().getUsername(),
                            v.getFechaVenta(),
                            v.getComprobante(),
                            v.getTipoComprobante().name(),
                            v.getSubtotalSinImpuesto(),
                            v.getImpuestoTotal(),
                            v.getTotal(),
                            v.getEstado().name(),

                            // --- MAPEO DE LOS NUEVOS CAMPOS ---
                            v.getCondicionPago().name(),
                            v.getEstadoPago().name(),
                            v.getFechaVencimiento(),
                            v.getSaldoPendiente(),

                            detallesDTO
                    );
                })
                .toList();
    }
}