package com.amtech.erp_saas_api.Controller;

import com.amtech.erp_saas_api.Entity.*;
import com.amtech.erp_saas_api.Repository.*;
import com.amtech.erp_saas_api.Security.ErpUserDetails;
import com.amtech.erp_saas_api.Service.ExcelExportService;
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
    private final ExcelExportService excelExportService;
    private final ProveedorRepository proveedorRepository;
    private final ClienteRepository clienteRepository;
    private final AbonoVentaRepository abonoVentaRepository;
    private final CompraRepository compraRepository;
    private final PagoProveedorRepository pagoProveedorRepository;
    @GetMapping("/ventas/excel")
    public ResponseEntity<Resource> reporteDeVentas(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Venta> ventas;
        if (inicio != null && fin != null) {
            ventas = ventaRepository.findVentasPorRangoFechas(userDetails.getEmpresaId(), inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        } else {
            ventas = ventaRepository.findByEmpresaIdOrderByFechaVentaDesc(userDetails.getEmpresaId());
        }

        ByteArrayInputStream stream = excelExportService.exportarVentas(ventas);
        return generarRespuestaExcel(stream, "ventas.xlsx");
    }

    @GetMapping("/kardex/excel")
    public ResponseEntity<Resource> reporteDeKardex(
            @AuthenticationPrincipal ErpUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) throws IOException {

        List<Kardex> kardex;
        if (inicio != null && fin != null) {
            kardex = kardexRepository.findKardexPorRangoFechas(userDetails.getEmpresaId(), inicio.atStartOfDay(), fin.atTime(23, 59, 59));
        } else {
            // Asumiendo que crearás un findAll por empresa en tu Repo
            throw new IllegalArgumentException("Debe enviar rango de fechas para el Kardex");
        }

        ByteArrayInputStream stream = excelExportService.exportarKardex(kardex);
        return generarRespuestaExcel(stream, "kardex_auditoria.xlsx");
    }
    @GetMapping("/proveedores/excel")
    public ResponseEntity<Resource> reporteDeProveedores(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        List<Proveedor> proveedores = proveedorRepository.findByEmpresaId(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarProveedores(proveedores);
        return generarRespuestaExcel(stream, "directorio_proveedores.xlsx");
    }
    @GetMapping("/clientes/excel")
    public ResponseEntity<Resource> reporteDeClientes(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        List<Cliente> clientes = clienteRepository.findByEmpresaId(userDetails.getEmpresaId());
        ByteArrayInputStream stream = excelExportService.exportarClientes(clientes);

        return generarRespuestaExcel(stream, "directorio_clientes.xlsx");
    }
    @GetMapping("/creditos/pendientes/excel")
    public ResponseEntity<Resource> reporteCuentasPorCobrar(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        // Reutilizamos la consulta que ya tenías para traer a los morosos
        List<Venta> deudas = ventaRepository.findVentasConDeudaPendiente(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarDeudasPendientes(deudas);
        return generarRespuestaExcel(stream, "cuentas_por_cobrar.xlsx");
    }

    // --- NUEVO ENDPOINT 2: HISTORIAL DE ABONOS ---
    @GetMapping("/creditos/abonos/excel")
    public ResponseEntity<Resource> reporteHistorialAbonos(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        List<AbonoVenta> abonos = abonoVentaRepository.findByEmpresaIdOrderByFechaPagoDesc(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarHistorialAbonos(abonos);
        return generarRespuestaExcel(stream, "historial_pagos_credito.xlsx");
    }
    @GetMapping("/compras/pendientes/excel")
    public ResponseEntity<Resource> reporteCuentasPorPagar(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        List<Compra> deudas = compraRepository.findComprasConDeudaPendiente(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarCuentasPorPagar(deudas);
        return generarRespuestaExcel(stream, "cuentas_por_pagar.xlsx");
    }

    @GetMapping("/compras/pagos/excel")
    public ResponseEntity<Resource> reporteHistorialPagosProveedores(
            @AuthenticationPrincipal ErpUserDetails userDetails) throws IOException {

        List<PagoProveedor> pagos = pagoProveedorRepository.findByEmpresaIdOrderByFechaPagoDesc(userDetails.getEmpresaId());

        ByteArrayInputStream stream = excelExportService.exportarHistorialPagosProveedores(pagos);
        return generarRespuestaExcel(stream, "historial_pagos_proveedores.xlsx");
    }

    private ResponseEntity<Resource> generarRespuestaExcel(ByteArrayInputStream stream, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }
}