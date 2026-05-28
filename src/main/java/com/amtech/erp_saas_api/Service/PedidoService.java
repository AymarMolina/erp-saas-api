package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.PedidoDetalleRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.PedidoRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.VentaDetalleRequestDTO;
import com.amtech.erp_saas_api.DTO.Request.VentaRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.PedidoResponseDTO;
import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final VentaService ventaService;

    @Transactional
    public PedidoResponseDTO crearPedido(Integer empresaId, PedidoRequestDTO request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(request.clienteId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Usuario vendedor = usuarioRepository.findByEmpresaIdAndRolId(empresaId, request.vendedorId())
                .stream().findFirst()
                .orElse(usuarioRepository.findById(request.vendedorId())
                        .orElseThrow(() -> new RuntimeException("Vendedor no encontrado")));

        Usuario embalador = null;
        if (request.embaladorId() != null) {
            embalador = usuarioRepository.findById(request.embaladorId())
                    .orElseThrow(() -> new RuntimeException("Embalador no encontrado"));
        }

        Pedido pedido = Pedido.builder()
                .empresa(empresa)
                .cliente(cliente)
                .vendedor(vendedor)
                .embalador(embalador)
                .estado(Pedido.EstadoPedido.PENDIENTE)
                .build();

        List<PedidoDetalle> detalles = new ArrayList<>();
        for (PedidoDetalleRequestDTO item : request.detalles()) {
            Producto producto = productoRepository.findByIdAndEmpresaId(item.productoId(), empresaId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.productoId()));

            // Validar stock disponible (físico - reservado)
            if (producto.getStockDisponible().compareTo(item.cantidad()) < 0) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                        + ". Disponible: " + producto.getStockDisponible());
            }

            PedidoDetalle detalle = PedidoDetalle.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioAcordado(item.precioAcordado())
                    .build();

            detalles.add(detalle);
        }

        pedido.setDetalle(detalles);
        return toDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO actualizarEstado(Integer empresaId, Integer pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findByIdAndEmpresaId(pedidoId, empresaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Pedido.EstadoPedido estadoAnterior = pedido.getEstado();
        Pedido.EstadoPedido nuevoEstadoEnum = Pedido.EstadoPedido.valueOf(nuevoEstado.toUpperCase());

        if (estadoAnterior == nuevoEstadoEnum) return toDTO(pedido);

        if (nuevoEstadoEnum == Pedido.EstadoPedido.ENTREGADO) {
            try {
                // 🔥 1. PRIMERO liberamos la reserva directa para que el VentaService vea el stock disponible
                for (PedidoDetalle detalle : pedido.getDetalle()) {
                    productoRepository.restarStockReservado(detalle.getProducto().getId(), detalle.getCantidad());
                }

                // 🔥 2. LUEGO registramos la venta (ahora pasará la validación de stock)
                VentaRequestDTO ventaRequest = convertirPedidoAVentaRequest(pedido);
                ventaService.registrarVenta(empresaId, pedido.getVendedor().getId(), ventaRequest);

            } catch (Exception e) {
                e.printStackTrace(); 
                throw new RuntimeException("Fallo al crear la factura automática: " + e.getMessage());
            }
        }
        else if (nuevoEstadoEnum == Pedido.EstadoPedido.CANCELADO) {
            for (PedidoDetalle detalle : pedido.getDetalle()) {
                productoRepository.restarStockReservado(detalle.getProducto().getId(), detalle.getCantidad());
            }
        }

        pedido.setEstado(nuevoEstadoEnum);
        return toDTO(pedidoRepository.save(pedido));
    }

    private VentaRequestDTO convertirPedidoAVentaRequest(Pedido pedido) {
        List<VentaDetalleRequestDTO> detalles = pedido.getDetalle().stream()
                .map(d -> new VentaDetalleRequestDTO(d.getProducto().getId(), d.getCantidad()))
                .toList();

        return new VentaRequestDTO(pedido.getCliente().getId(), Venta.TipoComprobante.FACTURA, detalles);
    }

    @Transactional
    public PedidoResponseDTO asignarEmbalador(Integer empresaId, Integer pedidoId, Integer embaladorId) {
        Pedido pedido = pedidoRepository.findByIdAndEmpresaId(pedidoId, empresaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Usuario embalador = usuarioRepository.findById(embaladorId)
                .orElseThrow(() -> new RuntimeException("Embalador no encontrado"));

        pedido.setEmbalador(embalador);
        pedido.setEstado(Pedido.EstadoPedido.EMPAQUETANDO);
        return toDTO(pedidoRepository.save(pedido));
    }

    public List<PedidoResponseDTO> listarPorEmpresa(Integer empresaId) {
        return pedidoRepository.findByEmpresaIdOrderByFechaPedidoDesc(empresaId)
                .stream().map(this::toDTO).toList();
    }

    public List<PedidoResponseDTO> listarPorEstado(Integer empresaId, String estado) {
        Pedido.EstadoPedido estadoEnum = Pedido.EstadoPedido.valueOf(estado.toUpperCase());
        return pedidoRepository.findPedidosParaFulfillment(empresaId, estadoEnum)
                .stream().map(this::toDTO).toList();
    }

    private PedidoResponseDTO toDTO(Pedido p) {
        List<PedidoResponseDTO.DetalleDTO> detallesDTO = p.getDetalle() != null
                ? p.getDetalle().stream().map(d -> new PedidoResponseDTO.DetalleDTO(
                d.getId(),
                d.getProducto().getId(),
                d.getProducto().getNombre(),
                d.getCantidad(),
                d.getPrecioAcordado(),
                d.getCantidad().multiply(d.getPrecioAcordado())
        )).toList()
                : List.of();

        return new PedidoResponseDTO(
                p.getId(),
                p.getEmpresa().getId(),
                p.getCliente().getId(),
                p.getCliente().getNombreCompleto(),
                p.getVendedor().getId(),
                p.getVendedor().getNombreCompleto(),
                p.getEmbalador() != null ? p.getEmbalador().getId() : null,
                p.getEmbalador() != null ? p.getEmbalador().getNombreCompleto() : null,
                p.getFechaPedido(),
                p.getEstado().name(),
                detallesDTO
        );
    }
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Integer empresaId, Integer id) {
        Pedido pedido = pedidoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // 1. Mapeamos la lista y calculamos el subtotal multiplicando en vivo
        List<PedidoResponseDTO.DetalleDTO> detallesDTO = pedido.getDetalle().stream()
                .map(d -> new PedidoResponseDTO.DetalleDTO(
                        d.getId(),
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioAcordado(),
                        d.getCantidad().multiply(d.getPrecioAcordado()) 
                )).toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getEmpresa().getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNombreCompleto(),
                pedido.getVendedor().getId(),
                pedido.getVendedor().getNombreCompleto(),
                pedido.getEmbalador() != null ? pedido.getEmbalador().getId() : null,
                pedido.getEmbalador() != null ? pedido.getEmbalador().getNombreCompleto() : null,
                pedido.getFechaPedido(),
                pedido.getEstado().name(),
                detallesDTO
        );
    }
}
