package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Request.AbonoRequestDTO;
import com.amtech.erp_saas_api.DTO.Response.AbonoResponseDTO;
import com.amtech.erp_saas_api.DTO.Response.VentaResponseDTO;
import com.amtech.erp_saas_api.Entity.AbonoVenta;
import com.amtech.erp_saas_api.Entity.Usuario;
import com.amtech.erp_saas_api.Entity.Venta;
import com.amtech.erp_saas_api.Repository.AbonoVentaRepository;
import com.amtech.erp_saas_api.Repository.UsuarioRepository;
import com.amtech.erp_saas_api.Repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoService {

    private final VentaRepository ventaRepository;
    private final AbonoVentaRepository abonoVentaRepository;
    private final UsuarioRepository usuarioRepository;

    // 1. Obtener todas las ventas que aún tienen saldo pendiente
    // 1. Obtener todas las ventas que aún tienen saldo pendiente
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> obtenerVentasConDeuda(Integer empresaId) {
        return ventaRepository.findVentasConDeudaPendiente(empresaId)
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

                    // Construimos el DTO principal de forma segura
                    return new VentaResponseDTO(
                            v.getId(),
                            v.getEmpresa().getId(),
                            v.getCliente() != null ? v.getCliente().getId() : null,
                            v.getCliente() != null ? v.getCliente().getNombreCompleto() : null,
                            v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : null, // <-- El documento que faltaba
                            v.getUsuario().getId(),
                            v.getUsuario().getUsername(),
                            v.getFechaVenta(),
                            v.getComprobante(),
                            v.getTipoComprobante().name(),
                            v.getSubtotalSinImpuesto(),
                            v.getImpuestoTotal(),
                            v.getTotal(),
                            v.getEstado().name(),
                            v.getCondicionPago().name(),
                            v.getEstadoPago().name(),
                            v.getFechaVencimiento(),
                            v.getSaldoPendiente(),
                            detallesDTO
                    );
                })
                .toList();
    }

    // 2. Registrar un nuevo pago/abono
    @Transactional
    public AbonoResponseDTO registrarAbono(Integer empresaId, Integer usuarioId, AbonoRequestDTO request) {

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        Venta venta = ventaRepository.findByIdAndEmpresaId(request.ventaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada o no pertenece a la empresa."));

        if (venta.getSaldoPendiente().compareTo(request.monto()) < 0) {
            throw new RuntimeException("El monto a pagar (S/ " + request.monto() +
                    ") no puede ser mayor al saldo pendiente (S/ " + venta.getSaldoPendiente() + ").");
        }

        AbonoVenta abono = AbonoVenta.builder()
                .venta(venta)
                .usuario(usuario)
                .monto(request.monto())
                .metodoPago(request.metodoPago())
                .referencia(request.referencia())
                .build();

        AbonoVenta abonoGuardado = abonoVentaRepository.save(abono);

        // Devolvemos el DTO limpio para evitar recursión
        return new AbonoResponseDTO(
                abonoGuardado.getId(),
                abonoGuardado.getVenta().getId(),
                abonoGuardado.getUsuario().getNombreCompleto(),
                abonoGuardado.getFechaPago(),
                abonoGuardado.getMonto(),
                abonoGuardado.getMetodoPago(),
                abonoGuardado.getReferencia()
        );
    }

    // 3. Ver el historial de pagos de una venta específica
    @Transactional(readOnly = true)
    public List<AbonoResponseDTO> obtenerHistorialAbonos(Integer ventaId) {
        return abonoVentaRepository.findByVentaIdOrderByFechaPagoDesc(ventaId)
                .stream()
                .map(a -> new AbonoResponseDTO(
                        a.getId(),
                        a.getVenta().getId(),
                        a.getUsuario().getNombreCompleto(),
                        a.getFechaPago(),
                        a.getMonto(),
                        a.getMetodoPago(),
                        a.getReferencia()
                )).toList();
    }
}