package com.amtech.erp_saas_api.DTO.Response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardResponseDTO {
    private Kpis kpis;
    private Alertas alertas;
    private List<GraficoVentas> graficoSieteDias;
    private List<VentaReciente> ventasRecientes;
    private List<Vencimiento> proximosVencimientos;

    @Data @Builder
    public static class Kpis {
        private BigDecimal ventasDelMes;
        private BigDecimal comprasDelMes;
        private BigDecimal porCobrar;
        private BigDecimal porPagar;
    }

    @Data @Builder
    public static class Alertas {
        private int pedidosPendientes;
        private List<ProductoStock> stockCritico;
    }

    @Data @Builder
    public static class ProductoStock {
        private Integer id;
        private String nombre;
        private BigDecimal stockFisico;
    }

    @Data @Builder
    public static class GraficoVentas {
        private LocalDate fecha;
        private BigDecimal ingresos;
        private BigDecimal egresos;
    }

    @Data @Builder
    public static class VentaReciente {
        private Integer id;
        private String comprobante;
        private String cliente;
        private BigDecimal total;
        private LocalDateTime fecha;
    }

    @Data @Builder
    public static class Vencimiento {
        private String tipo; // "COBRAR" o "PAGAR"
        private String comprobante;
        private String entidad; // Cliente o Proveedor
        private BigDecimal monto;
        private LocalDate fechaVencimiento;
    }
}