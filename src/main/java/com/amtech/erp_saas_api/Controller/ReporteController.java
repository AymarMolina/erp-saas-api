package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ExcelExportService;
import com.amtech.erp_saas_api.Service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final VentaRepository ventaRepository;
    private final KardexRepository kardexRepository;
    private final ProveedorRepository proveedorRepository;
    private final ClienteRepository clienteRepository;
    private final AbonoVentaRepository abonoVentaRepository;
    private final CompraRepository compraRepository;
    private final PagoProveedorRepository pagoProveedorRepository;
    private final EmpresaRepository empresaRepository;

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    // ==========================================
    // 1. VENTAS
    // ==========================================
    @GetMapping("/ventas/excel")
    public ResponseEntity<Resource> reporteDeVentasExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Venta> ventas = obtenerVentas(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarVentas(ventas, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "ventas.xlsx");
    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<Resource> reporteDeVentasPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<Venta> ventas = obtenerVentas(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        // Actualizado con los nuevos parámetros
        ByteArrayInputStream stream = pdfExportService.exportarVentasPdf(ventas, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "ventas.pdf");
    }
    // ==========================================
    // 9. COMPRAS (Registro General)
    // ==========================================
    @GetMapping("/compras/excel")
    public ResponseEntity<Resource> reporteDeComprasExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Compra> compras = obtenerCompras(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarCompras(compras, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "registro_compras.xlsx");
    }

    @GetMapping("/compras/pdf")
    public ResponseEntity<Resource> reporteDeComprasPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<Compra> compras = obtenerCompras(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarComprasPdf(compras, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "registro_compras.pdf");
    }

    // Pon este helper abajo, junto a tus otros métodos "obtener..."
    private List<Compra> obtenerCompras(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return compraRepository.findComprasPorRangoDeFechas(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return compraRepository.findByEmpresaIdOrderByFechaCompraDesc(empresaId);
    }
    // ==========================================
// 2. KARDEX
// ==========================================
    @GetMapping("/kardex/excel")
    public ResponseEntity<Resource> reporteDeKardexExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) Integer productoId) throws IOException { // <-- NUEVO PARÁMETRO

        // Pasamos el productoId a la consulta
        List<Kardex> kardex = obtenerKardex(userDetails.getEmpresaId(), inicio, fin, productoId);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarKardex(kardex, logoUrl, inicio, fin);
        System.out.println("TAMAÑO DE LA LISTA PARA EXCEL: " + kardex.size()); // <-- AGREGA ESTO
        return generarRespuestaExcel(stream, "kardex.xlsx");
    }

    @GetMapping("/kardex/pdf")
    public ResponseEntity<Resource> reporteDeKardexPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) Integer productoId) { // <-- NUEVO PARÁMETRO

        // Pasamos el productoId a la consulta
        List<Kardex> kardex = obtenerKardex(userDetails.getEmpresaId(), inicio, fin, productoId);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarKardexPdf(kardex, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "kardex.pdf");
    }

    // ==========================================
    // 3. PROVEEDORES
    // ==========================================
    @GetMapping("/proveedores/excel")
    public ResponseEntity<Resource> reporteDeProveedoresExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Proveedor> proveedores = proveedorRepository.findByEmpresaId(userDetails.getEmpresaId());
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarProveedores(proveedores, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "proveedores.xlsx");
    }

    @GetMapping("/proveedores/pdf")
    public ResponseEntity<Resource> reporteDeProveedoresPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<Proveedor> proveedores = proveedorRepository.findByEmpresaId(userDetails.getEmpresaId());
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        // Actualizado con los nuevos parámetros
        ByteArrayInputStream stream = pdfExportService.exportarProveedoresPdf(proveedores, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "proveedores.pdf");
    }

    // ==========================================
    // 4. CLIENTES
    // ==========================================
    @GetMapping("/clientes/excel")
    public ResponseEntity<Resource> reporteDeClientesExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Cliente> clientes = clienteRepository.findByEmpresaId(userDetails.getEmpresaId());
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarClientes(clientes, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "clientes.xlsx");
    }

    @GetMapping("/clientes/pdf")
    public ResponseEntity<Resource> reporteDeClientesPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<Cliente> clientes = clienteRepository.findByEmpresaId(userDetails.getEmpresaId());
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        // Actualizado con los nuevos parámetros
        ByteArrayInputStream stream = pdfExportService.exportarClientesPdf(clientes, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "clientes.pdf");
    }

    // ==========================================
    // 5. CUENTAS POR COBRAR (Deudas Ventas)
    // ==========================================
    @GetMapping("/creditos/pendientes/excel")
    public ResponseEntity<Resource> reporteCuentasPorCobrarExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Venta> deudas = obtenerDeudasVentas(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarDeudasPendientes(deudas, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "cuentas_cobrar.xlsx");
    }

    @GetMapping("/creditos/pendientes/pdf")
    public ResponseEntity<Resource> reporteCuentasPorCobrarPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // Cambiado para usar el helper y respetar las fechas
        List<Venta> deudas = obtenerDeudasVentas(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarDeudasPendientesPdf(deudas, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "cuentas_cobrar.pdf");
    }

    // ==========================================
    // 6. HISTORIAL DE ABONOS (Ventas)
    // ==========================================
    @GetMapping("/creditos/abonos/excel")
    public ResponseEntity<Resource> reporteHistorialAbonosExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<AbonoVenta> abonos = obtenerAbonos(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarHistorialAbonos(abonos, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "historial_cobros.xlsx");
    }

    @GetMapping("/creditos/abonos/pdf")
    public ResponseEntity<Resource> reporteHistorialAbonosPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // Cambiado para usar el helper y respetar las fechas
        List<AbonoVenta> abonos = obtenerAbonos(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarHistorialAbonosPdf(abonos, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "historial_cobros.pdf");
    }

    // ==========================================
    // 7. CUENTAS POR PAGAR (Deudas Compras)
    // ==========================================
    @GetMapping("/compras/pendientes/excel")
    public ResponseEntity<Resource> reporteCuentasPorPagarExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Compra> deudas = obtenerDeudasCompras(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarCuentasPorPagar(deudas, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "cuentas_pagar.xlsx");
    }

    @GetMapping("/compras/pendientes/pdf")
    public ResponseEntity<Resource> reporteCuentasPorPagarPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // Cambiado para usar el helper y respetar las fechas
        List<Compra> deudas = obtenerDeudasCompras(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarCuentasPorPagarPdf(deudas, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "cuentas_pagar.pdf");
    }

    // ==========================================
    // 8. HISTORIAL DE PAGOS (Proveedores)
    // ==========================================
    @GetMapping("/compras/pagos/excel")
    public ResponseEntity<Resource> reporteHistorialPagosProveedoresExcel(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<PagoProveedor> pagos = obtenerPagosProveedores(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarHistorialPagosProveedores(pagos, logoUrl, inicio, fin);
        return generarRespuestaExcel(stream, "historial_pagos.xlsx");
    }

    @GetMapping("/compras/pagos/pdf")
    public ResponseEntity<Resource> reporteHistorialPagosProveedoresPdf(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // Cambiado para usar el helper y respetar las fechas
        List<PagoProveedor> pagos = obtenerPagosProveedores(userDetails.getEmpresaId(), inicio, fin);
        String logoUrl = obtenerLogoUrl(userDetails.getEmpresaId());

        ByteArrayInputStream stream = pdfExportService.exportarHistorialPagosProveedoresPdf(pagos, logoUrl, inicio, fin);
        return generarRespuestaPdf(stream, "historial_pagos.pdf");
    }

    // ==========================================
    // HELPERS (Lógica reutilizable)
    // ==========================================

    private String obtenerLogoUrl(Integer empresaId) {
        return empresaRepository.findById(empresaId)
                .map(Empresa::getLogoUrl)
                .orElse(null);
    }

    private List<Venta> obtenerVentas(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return ventaRepository.findVentasPorRangoFechas(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return ventaRepository.findByEmpresaIdOrderByFechaVentaDesc(empresaId);
    }

    private List<Kardex> obtenerKardex(Integer empresaId, LocalDate inicio, LocalDate fin, Integer productoId) {
        if (inicio != null && fin != null) {
            LocalDateTime fechaInicio = inicio.atStartOfDay();
            LocalDateTime fechaFin = fin.atTime(23, 59, 59);

            // Si enviaron un producto específico, filtramos por él
            if (productoId != null) {
                return kardexRepository.findKardexPorRangoFechasYProducto(empresaId, productoId, fechaInicio, fechaFin);
            }
            // Si no enviaron producto, traemos el kardex global de toda la empresa
            else {
                return kardexRepository.findKardexPorRangoFechas(empresaId, fechaInicio, fechaFin);
            }
        }
        throw new IllegalArgumentException("Debe enviar rango de fechas para el Kardex");
    }

    private List<Venta> obtenerDeudasVentas(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return ventaRepository.findVentasConDeudaPendientePorRangoFechas(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return ventaRepository.findVentasConDeudaPendiente(empresaId);
    }

    private List<AbonoVenta> obtenerAbonos(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return abonoVentaRepository.findByEmpresaIdAndFechaPagoBetweenOrderByFechaPagoDesc(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return abonoVentaRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
    }

    private List<Compra> obtenerDeudasCompras(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return compraRepository.findComprasConDeudaPendientePorRangoFechas(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return compraRepository.findComprasConDeudaPendiente(empresaId);
    }

    private List<PagoProveedor> obtenerPagosProveedores(Integer empresaId, LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null) {
            return pagoProveedorRepository.findByEmpresaIdAndFechaPagoBetweenOrderByFechaPagoDesc(empresaId, inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        }
        return pagoProveedorRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
    }

    private ResponseEntity<Resource> generarRespuestaExcel(ByteArrayInputStream stream, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    private ResponseEntity<Resource> generarRespuestaPdf(ByteArrayInputStream stream, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}