package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.DTO.Response.DashboardResponseDTO;
import com.amtech.erp_saas_api.Entity.Compra;
import com.amtech.erp_saas_api.Entity.Venta;
import com.amtech.erp_saas_api.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO obtenerDatosDashboard(Integer empresaId) {
        LocalDate hoy = LocalDate.now();
        int mesAct = hoy.getMonthValue();
        int anioAct = hoy.getYear();

        // 1. KPIS PRINCIPALES
        DashboardResponseDTO.Kpis kpis = DashboardResponseDTO.Kpis.builder()
                .ventasDelMes(ventaRepository.sumarVentasDelMes(empresaId, anioAct, mesAct))
                .comprasDelMes(compraRepository.sumarComprasDelMes(empresaId, anioAct, mesAct))
                .porCobrar(ventaRepository.sumarCuentasPorCobrar(empresaId))
                .porPagar(compraRepository.sumarCuentasPorPagar(empresaId))
                .build();

        // 2. ALERTAS OPERATIVAS
        int pedidosPendientes = pedidoRepository.contarPedidosPendientes(empresaId);

        List<DashboardResponseDTO.ProductoStock> stockCritico = productoRepository.findTop5StockCritico(empresaId, PageRequest.of(0, 5))
                .stream()
                .map(p -> DashboardResponseDTO.ProductoStock.builder()
                        .id(p.getId())
                        .nombre(p.getNombre())
                        .stockFisico(p.getStockFisico())
                        .build())
                .toList();

        DashboardResponseDTO.Alertas alertas = DashboardResponseDTO.Alertas.builder()
                .pedidosPendientes(pedidosPendientes)
                .stockCritico(stockCritico)
                .build();

        // 3. GRÁFICO (Últimos 7 días)
        List<DashboardResponseDTO.GraficoVentas> grafico = armarGraficoSieteDias(empresaId, hoy);

        // 4. VENTAS RECIENTES
        List<DashboardResponseDTO.VentaReciente> ventasRecientes = ventaRepository.findTop5ByEmpresaIdAndEstadoOrderByFechaVentaDesc(empresaId, Venta.EstadoVenta.COMPLETADA)
                .stream()
                .map(v -> DashboardResponseDTO.VentaReciente.builder()
                        .id(v.getId())
                        .comprobante(v.getComprobante())
                        .cliente(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General")
                        .total(v.getTotal())
                        .fecha(v.getFechaVenta())
                        .build())
                .toList();

        // 5. VENCIMIENTOS (Mezclar compras y ventas que vencen en los próximos 5 días)
        List<DashboardResponseDTO.Vencimiento> vencimientos = obtenerProximosVencimientos(empresaId, hoy.plusDays(5));

        return DashboardResponseDTO.builder()
                .kpis(kpis)
                .alertas(alertas)
                .graficoSieteDias(grafico)
                .ventasRecientes(ventasRecientes)
                .proximosVencimientos(vencimientos)
                .build();
    }

    private List<DashboardResponseDTO.GraficoVentas> armarGraficoSieteDias(Integer empresaId, LocalDate hoy) {
        List<DashboardResponseDTO.GraficoVentas> data = new ArrayList<>();
        // Retrocedemos 6 días para que sean 7 contando hoy
        LocalDate fechaInicio = hoy.minusDays(6);

        // Obtener la data de la DB
        List<Venta> ventasSemana = ventaRepository.findVentasPorRangoFechas(empresaId, fechaInicio.atStartOfDay(), hoy.atTime(23, 59, 59));
        List<Compra> comprasSemana = compraRepository.findComprasPorRangoDeFechas(empresaId, fechaInicio.atStartOfDay(), hoy.atTime(23, 59, 59));

        for (int i = 0; i <= 6; i++) {
            LocalDate diaEnCuestion = fechaInicio.plusDays(i);

            // Sumar ventas de ese día
            BigDecimal sumaVentas = ventasSemana.stream()
                    .filter(v -> v.getFechaVenta().toLocalDate().equals(diaEnCuestion) && v.getEstado() == Venta.EstadoVenta.COMPLETADA)
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sumar compras de ese día
            BigDecimal sumaCompras = comprasSemana.stream()
                    .filter(c -> c.getFechaCompra().toLocalDate().equals(diaEnCuestion) && c.getEstado() == Compra.EstadoCompra.COMPLETADA)
                    .map(Compra::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            data.add(DashboardResponseDTO.GraficoVentas.builder()
                    .fecha(diaEnCuestion)
                    .ingresos(sumaVentas)
                    .egresos(sumaCompras)
                    .build());
        }
        return data;
    }

    private List<DashboardResponseDTO.Vencimiento> obtenerProximosVencimientos(Integer empresaId, LocalDate limite) {
        List<DashboardResponseDTO.Vencimiento> lista = new ArrayList<>();

        List<Venta> deudasClientes = ventaRepository.findVencimientosProximos(empresaId, limite);
        for (Venta v : deudasClientes) {
            lista.add(DashboardResponseDTO.Vencimiento.builder()
                    .tipo("COBRAR")
                    .comprobante(v.getComprobante())
                    .entidad(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Cliente")
                    .monto(v.getSaldoPendiente())
                    .fechaVencimiento(v.getFechaVencimiento())
                    .build());
        }

        List<Compra> deudasProveedores = compraRepository.findVencimientosProximos(empresaId, limite);
        for (Compra c : deudasProveedores) {
            lista.add(DashboardResponseDTO.Vencimiento.builder()
                    .tipo("PAGAR")
                    .comprobante(c.getComprobante())
                    .entidad(c.getProveedor().getRazonSocial())
                    .monto(c.getSaldoPendiente())
                    .fechaVencimiento(c.getFechaVencimiento())
                    .build());
        }

        // Ordenamos la lista mixta por fecha de vencimiento (los más urgentes primero)
        lista.sort(Comparator.comparing(DashboardResponseDTO.Vencimiento::getFechaVencimiento));

        // Devolvemos solo los 6 más urgentes para no llenar la pantalla
        return lista.stream().limit(6).toList();
    }
}