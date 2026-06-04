package com.amtech.erp_saas_api.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.amtech.erp_saas_api.Entity.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportarVentas(List<Venta> ventas, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registro de Ventas");
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "REGISTRO DE VENTAS", logoUrl, inicio, fin);

            Row header = sheet.createRow(5);
            // Cabeceras actualizadas (14 columnas)
            String[] columnas = {"ID", "Fecha", "Comprobante", "Tipo", "Cliente", "Documento", "Condición", "Subtotal", "IGV 18%", "IGV 10%", "Inafecto", "Total", "Cajero", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(v.getFechaVenta()); row.getCell(1).setCellStyle(dateStyle);
                row.createCell(2).setCellValue(v.getComprobante()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(v.getTipoComprobante().name()); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General"); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : "-"); row.getCell(5).setCellStyle(normalStyle);
                row.createCell(6).setCellValue(obtenerCondicionYPlazo(v)); row.getCell(6).setCellStyle(normalStyle);

                // --- LÓGICA DE CÁLCULO ---
                double subtotal = v.getSubtotalSinImpuesto().doubleValue();
                double igv = v.getImpuestoTotal().doubleValue();
                long porcentajeIgv = subtotal > 0 ? Math.round((igv / subtotal) * 100) : 0;

                // Columna 7: Subtotal Gravado (Solo se llena si hay IGV)
                Cell cellSubtotal = row.createCell(7);
                if (porcentajeIgv > 0) {
                    cellSubtotal.setCellValue(subtotal);
                    cellSubtotal.setCellStyle(moneyStyle);
                } else {
                    cellSubtotal.setCellValue("");
                    cellSubtotal.setCellStyle(normalStyle);
                }

                // Columna 8: IGV 18%
                Cell cell18 = row.createCell(8);
                if (porcentajeIgv == 18) { cell18.setCellValue(igv); cell18.setCellStyle(moneyStyle); }
                else { cell18.setCellValue(""); cell18.setCellStyle(normalStyle); }

                // Columna 9: IGV 10%
                Cell cell10 = row.createCell(9);
                if (porcentajeIgv == 10) { cell10.setCellValue(igv); cell10.setCellStyle(moneyStyle); }
                else { cell10.setCellValue(""); cell10.setCellStyle(normalStyle); }

                // Columna 10: Inafecto (Monto base directo aquí, sin IGV)
                Cell cell0 = row.createCell(10);
                if (porcentajeIgv == 0) { cell0.setCellValue(subtotal); cell0.setCellStyle(moneyStyle); }
                else { cell0.setCellValue(""); cell0.setCellStyle(normalStyle); }
                // -------------------------

                // Columna 11: Total Fijo
                row.createCell(11).setCellValue(v.getTotal().doubleValue()); row.getCell(11).setCellStyle(moneyStyle);

                row.createCell(12).setCellValue(v.getUsuario().getNombreCompleto()); row.getCell(12).setCellStyle(normalStyle);
                row.createCell(13).setCellValue(v.getEstado().name()); row.getCell(13).setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    public ByteArrayInputStream exportarCompras(List<Compra> compras, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registro de Compras");
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "REGISTRO OFICIAL DE COMPRAS", logoUrl, inicio, fin);

            Row header = sheet.createRow(5);
            // 13 columnas en total para Excel
            String[] columnas = {"ID", "Fecha", "Comprobante", "Proveedor", "RUC/DNI", "Condición", "Base Gravada", "IGV 18%", "IGV 10%", "Inafecto", "Total", "Cajero", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Compra c : compras) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(c.getFechaCompra()); row.getCell(1).setCellStyle(dateStyle);
                row.createCell(2).setCellValue(c.getComprobante()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(c.getProveedor().getRazonSocial()); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(c.getProveedor().getDocumentoIdentidad()); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(c.getCondicionPago().name()); row.getCell(5).setCellStyle(normalStyle);

                // --- LÓGICA DE DEDUCCIÓN DEL IGV ---
                double subtotal = c.getSubtotalSinImpuesto().doubleValue();
                double igv = c.getImpuestoTotal().doubleValue();
                long porcentajeIgv = subtotal > 0 ? Math.round((igv / subtotal) * 100) : 0;

                // Columna 6: Base Gravada
                Cell cellSub = row.createCell(6);
                if (porcentajeIgv > 0) { cellSub.setCellValue(subtotal); cellSub.setCellStyle(moneyStyle); }
                else { cellSub.setCellValue(""); cellSub.setCellStyle(normalStyle); }

                // Columna 7: IGV 18%
                Cell cell18 = row.createCell(7);
                if (porcentajeIgv == 18) { cell18.setCellValue(igv); cell18.setCellStyle(moneyStyle); }
                else { cell18.setCellValue(""); cell18.setCellStyle(normalStyle); }

                // Columna 8: IGV 10%
                Cell cell10 = row.createCell(8);
                if (porcentajeIgv == 10) { cell10.setCellValue(igv); cell10.setCellStyle(moneyStyle); }
                else { cell10.setCellValue(""); cell10.setCellStyle(normalStyle); }

                // Columna 9: Inafecto
                Cell cell0 = row.createCell(9);
                if (porcentajeIgv == 0) { cell0.setCellValue(subtotal); cell0.setCellStyle(moneyStyle); }
                else { cell0.setCellValue(""); cell0.setCellStyle(normalStyle); }
                // -----------------------------------

                row.createCell(10).setCellValue(c.getTotal().doubleValue()); row.getCell(10).setCellStyle(moneyStyle);
                row.createCell(11).setCellValue(c.getUsuario().getNombreCompleto()); row.getCell(11).setCellStyle(normalStyle);
                row.createCell(12).setCellValue(c.getEstado().name()); row.getCell(12).setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    // --- 2. EXPORTAR KARDEX (Agrupado por días y ordenado cronológicamente) ---
    // --- 2. EXPORTAR KARDEX (Agrupado por días y ordenado cronológicamente) ---
    public ByteArrayInputStream exportarKardex(List<Kardex> movimientos, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Kardex");
            CreationHelper helper = workbook.getCreationHelper();

            // 1. Cargar estilos (Mantenidos igual)
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);
            CellStyle numberStyle = crearEstiloBordes(workbook);
            numberStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0.00"));
            CellStyle groupStyle = crearEstiloGrupo(workbook);

            // 2. Encabezado y logo
            insertarEncabezadoConLogo(workbook, sheet, "AUDITORÍA DE KARDEX", logoUrl, inicio, fin);

            // 3. Cabeceras de tabla
            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Fecha y Hora", "Tipo", "Motivo", "Producto", "Lote", "Cantidad", "Saldo Físico"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Ordenar los movimientos
            movimientos.sort(Comparator.comparing(Kardex::getFechaMovimiento));

            int rowIdx = 6;
            LocalDate fechaActual = null;
            java.time.format.DateTimeFormatter formateadorDia = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // 5. Imprimir datos
            for (Kardex k : movimientos) {
                LocalDate fechaDelMovimiento = k.getFechaMovimiento().toLocalDate();

                if (fechaActual == null || !fechaActual.equals(fechaDelMovimiento)) {
                    fechaActual = fechaDelMovimiento;
                    Row groupRow = sheet.createRow(rowIdx++);
                    Cell groupCell = groupRow.createCell(0);
                    groupCell.setCellValue("MOVIMIENTOS DEL DÍA: " + fechaActual.format(formateadorDia));
                    groupCell.setCellStyle(groupStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, columnas.length - 1));
                }

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(k.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(k.getFechaMovimiento()); row.getCell(1).setCellStyle(dateStyle);
                row.createCell(2).setCellValue(k.getTipoMovimiento().name()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(k.getMotivo()); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(k.getLote().getProducto().getNombre()); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(k.getLote().getCodigoLote()); row.getCell(5).setCellStyle(normalStyle);
                row.createCell(6).setCellValue(k.getCantidad().doubleValue()); row.getCell(6).setCellStyle(numberStyle);
                row.createCell(7).setCellValue(k.getSaldoLote().doubleValue()); row.getCell(7).setCellStyle(numberStyle);
            }

            // ==============================================================
            // NUEVO: RESUMEN DE STOCK AL FINAL DEL EXCEL
            // ==============================================================
            if (!movimientos.isEmpty()) {
                rowIdx += 2; // Dejar 2 filas en blanco

                // Título del resumen
                Row summaryTitle = sheet.createRow(rowIdx++);
                Cell titleCell = summaryTitle.createCell(0);
                titleCell.setCellValue("RESUMEN DE STOCK ACTUAL (Lotes involucrados)");
                titleCell.setCellStyle(groupStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 2));

                // Cabeceras del resumen
                Row summaryHeaderRow = sheet.createRow(rowIdx++);
                summaryHeaderRow.createCell(0).setCellValue("Producto"); summaryHeaderRow.getCell(0).setCellStyle(headerStyle);
                summaryHeaderRow.createCell(1).setCellValue("Lote"); summaryHeaderRow.getCell(1).setCellStyle(headerStyle);
                summaryHeaderRow.createCell(2).setCellValue("Stock Actual"); summaryHeaderRow.getCell(2).setCellStyle(headerStyle);

                // Filtrar lotes únicos para no repetirlos
                Map<Integer, Kardex> lotesUnicos = new HashMap<>();
                for (Kardex k : movimientos) {
                    if (k.getLote() != null) lotesUnicos.put(k.getLote().getId(), k);
                }

                // Imprimir los lotes y sumar el total
                double stockTotal = 0.0;
                for (Kardex k : lotesUnicos.values()) {
                    Row r = sheet.createRow(rowIdx++);
                    r.createCell(0).setCellValue(k.getLote().getProducto().getNombre()); r.getCell(0).setCellStyle(normalStyle);
                    r.createCell(1).setCellValue(k.getLote().getCodigoLote()); r.getCell(1).setCellStyle(normalStyle);

                    double cantActual = k.getLote().getCantidadActual().doubleValue();
                    r.createCell(2).setCellValue(cantActual); r.getCell(2).setCellStyle(numberStyle);
                    stockTotal += cantActual;
                }

                // Fila de Total
                Row totalRow = sheet.createRow(rowIdx++);
                totalRow.createCell(1).setCellValue("TOTAL STOCK:"); totalRow.getCell(1).setCellStyle(headerStyle);
                totalRow.createCell(2).setCellValue(stockTotal); totalRow.getCell(2).setCellStyle(headerStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    public ByteArrayInputStream exportarProveedores(List<Proveedor> proveedores,String logoUrl,  LocalDate inicio,LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Proveedores");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "DIRECTORIO DE PROVEEDORES",logoUrl ,inicio, fin);

            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Razón Social", "RUC/DNI", "Teléfono", "Email"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Proveedor p : proveedores) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(p.getRazonSocial()); row.getCell(1).setCellStyle(normalStyle);
                row.createCell(2).setCellValue(p.getDocumentoIdentidad()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(p.getTelefono() != null ? p.getTelefono() : ""); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(p.getEmail() != null ? p.getEmail() : ""); row.getCell(4).setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    public ByteArrayInputStream exportarClientes(List<Cliente> clientes,String logoUrl,LocalDate inicio ,LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clientes");
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "DIRECTORIO DE CLIENTES",logoUrl, inicio, fin);

            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Documento", "Nombre/Razón Social", "Teléfono", "Email", "Dirección", "Límite de Crédito"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Cliente c : clientes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(c.getDocumentoIdentidad()); row.getCell(1).setCellStyle(normalStyle);
                row.createCell(2).setCellValue(c.getNombreCompleto()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(c.getTelefono() != null ? c.getTelefono() : ""); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(c.getEmail() != null ? c.getEmail() : ""); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(c.getDireccion() != null ? c.getDireccion() : ""); row.getCell(5).setCellStyle(normalStyle);
                row.createCell(6).setCellValue(c.getLimiteCredito() != null ? c.getLimiteCredito().doubleValue() : 0.0); row.getCell(6).setCellStyle(moneyStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    public ByteArrayInputStream exportarDeudasPendientes(List<Venta> deudas, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cuentas por Cobrar");
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "REPORTE DE CUENTAS POR COBRAR", logoUrl, inicio, fin);

            Row header = sheet.createRow(5);
            // NUEVO: Columna "Plazo Otorgado"
            String[] columnas = {"Comprobante", "Fecha Emisión", "Vencimiento", "Cliente", "Documento", "Total Facturado", "Saldo Pendiente", "Plazo Otorgado", "Estado", "Alerta Vencimiento"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Venta v : deudas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getComprobante()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(v.getFechaVenta()); row.getCell(1).setCellStyle(dateStyle);

                Cell c2 = row.createCell(2);
                if (v.getFechaVencimiento() != null) {
                    c2.setCellValue(java.sql.Date.valueOf(v.getFechaVencimiento())); c2.setCellStyle(dateStyle);
                } else {
                    c2.setCellValue("N/A"); c2.setCellStyle(normalStyle);
                }

                row.createCell(3).setCellValue(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General"); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : "N/A"); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(v.getTotal().doubleValue()); row.getCell(5).setCellStyle(moneyStyle);
                row.createCell(6).setCellValue(v.getSaldoPendiente().doubleValue()); row.getCell(6).setCellStyle(moneyStyle);

                // NUEVO: Días otorgados
                row.createCell(7).setCellValue(obtenerTextoPlazo(v.getFechaVenta().toLocalDate(), v.getFechaVencimiento())); row.getCell(7).setCellStyle(normalStyle);

                row.createCell(8).setCellValue(v.getEstadoPago().name()); row.getCell(8).setCellStyle(normalStyle);
                row.createCell(9).setCellValue(calcularAlertaVencimientoVenta(v)); row.getCell(9).setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }



    public ByteArrayInputStream exportarHistorialAbonos(List<AbonoVenta> abonos, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historial Cobros");
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            CellStyle infoLabelStyle = crearEstiloInfoEtiqueta(workbook);
            CellStyle infoValueStyle = crearEstiloInfoValor(workbook);
            CellStyle infoMoneyStyle = crearEstiloInfoMoneda(workbook, helper);

            insertarEncabezadoConLogo(workbook, sheet, "HISTORIAL DE COBROS A CLIENTES", logoUrl, inicio, fin);

            abonos.sort(Comparator.comparing((AbonoVenta a) -> a.getVenta().getComprobante())
                    .thenComparing(AbonoVenta::getFechaPago));

            int rowIdx = 5;
            String ultimoComprobante = "";

            for (AbonoVenta a : abonos) {
                Venta v = a.getVenta();

                if (!v.getComprobante().equals(ultimoComprobante)) {

                    if (!ultimoComprobante.isEmpty()) {
                        rowIdx++;
                    }

                    Row infoRow1 = sheet.createRow(rowIdx++);

                    Cell ir1c0 = infoRow1.createCell(0); ir1c0.setCellValue("COMPROBANTE:"); ir1c0.setCellStyle(infoLabelStyle);
                    Cell ir1c1 = infoRow1.createCell(1); ir1c1.setCellValue(v.getComprobante()); ir1c1.setCellStyle(infoValueStyle);

                    Cell ir1c2 = infoRow1.createCell(2); ir1c2.setCellValue("CLIENTE:"); ir1c2.setCellStyle(infoLabelStyle);

                        // --- INICIO DE LA MODIFICACIÓN ---
                                            String nombreCliente = "Público General";
                                            if (v.getCliente() != null) {
                                                String nombre = v.getCliente().getNombreCompleto();
                                                String documento = v.getCliente().getDocumentoIdentidad(); // Asumiendo que tu getter se llama así

                                                if (documento != null && !documento.trim().isEmpty()) {
                                                    nombreCliente = nombre + " (Doc: " + documento + ")";
                                                } else {
                                                    nombreCliente = nombre;
                                                }
                                            }
                        // --- FIN DE LA MODIFICACIÓN ---

                    Cell ir1c3 = infoRow1.createCell(3); ir1c3.setCellValue(nombreCliente); ir1c3.setCellStyle(infoValueStyle);
                    Cell ir1c4 = infoRow1.createCell(4); ir1c4.setCellValue("ALERTA:"); ir1c4.setCellStyle(infoLabelStyle);
                    Cell ir1c5 = infoRow1.createCell(5); ir1c5.setCellValue(calcularAlertaVencimientoVenta(v)); ir1c5.setCellStyle(infoValueStyle);

                    // --- FILA 2 DEL BLOQUE: Saldos ---
                    Row infoRow2 = sheet.createRow(rowIdx++);

                    Cell ir2c0 = infoRow2.createCell(0); ir2c0.setCellValue("Total Facturado:"); ir2c0.setCellStyle(infoLabelStyle);
                    Cell ir2c1 = infoRow2.createCell(1); ir2c1.setCellValue(v.getTotal().doubleValue()); ir2c1.setCellStyle(infoMoneyStyle);

                    Cell ir2c2 = infoRow2.createCell(2); ir2c2.setCellValue("Saldo Restante:"); ir2c2.setCellStyle(infoLabelStyle);
                    Cell ir2c3 = infoRow2.createCell(3); ir2c3.setCellValue(v.getSaldoPendiente().doubleValue()); ir2c3.setCellStyle(infoMoneyStyle);

                    // NUEVO: En lugar de celdas vacías, mostramos el Plazo
                    Cell ir2c4 = infoRow2.createCell(4); ir2c4.setCellValue("PLAZO:"); ir2c4.setCellStyle(infoLabelStyle);
                    Cell ir2c5 = infoRow2.createCell(5); ir2c5.setCellValue(obtenerTextoPlazo(v.getFechaVenta().toLocalDate(), v.getFechaVencimiento())); ir2c5.setCellStyle(infoValueStyle);

                    // ¡ELIMINAMOS EL sheet.addMergedRegion AQUÍ!

                    Row subHeaderRow = sheet.createRow(rowIdx++);
                    String[] columnas = {"ID Pago", "Fecha de Abono", "Método", "Nro. Referencia", "Monto Abonado", "Cajero Registrador"};
                    for (int i = 0; i < columnas.length; i++) {
                        Cell cell = subHeaderRow.createCell(i);
                        cell.setCellValue(columnas[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    ultimoComprobante = v.getComprobante();
                }

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(a.getFechaPago()); row.getCell(1).setCellStyle(dateStyle);
                row.createCell(2).setCellValue(a.getMetodoPago()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(a.getReferencia() != null ? a.getReferencia() : ""); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(a.getMonto().doubleValue()); row.getCell(4).setCellStyle(moneyStyle);
                row.createCell(5).setCellValue(a.getUsuario().getNombreCompleto()); row.getCell(5).setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, 6);
            return escribirLibro(workbook);
        }
    }

    public ByteArrayInputStream exportarCuentasPorPagar(List<Compra> deudas, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cuentas por Pagar");
            CreationHelper helper = workbook.getCreationHelper();
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "CUENTAS POR PAGAR (PROVEEDORES)", logoUrl, inicio, fin);

            Row header = sheet.createRow(5);
            // NUEVO: Columna "Plazo Otorgado"
            String[] columnas = {"Factura", "Fecha Emisión", "Vencimiento", "Proveedor", "RUC/DNI", "Total Facturado", "Saldo Pendiente", "Plazo Otorgado", "Estado Pago", "Alerta Vencimiento"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Compra c : deudas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getComprobante()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(c.getFechaCompra()); row.getCell(1).setCellStyle(dateStyle);

                Cell c2 = row.createCell(2);
                if (c.getFechaVencimiento() != null) {
                    c2.setCellValue(java.sql.Date.valueOf(c.getFechaVencimiento())); c2.setCellStyle(dateStyle);
                } else {
                    c2.setCellValue("N/A"); c2.setCellStyle(normalStyle);
                }

                row.createCell(3).setCellValue(c.getProveedor().getRazonSocial()); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(c.getProveedor().getDocumentoIdentidad()); row.getCell(4).setCellStyle(normalStyle);
                row.createCell(5).setCellValue(c.getTotal().doubleValue()); row.getCell(5).setCellStyle(moneyStyle);
                row.createCell(6).setCellValue(c.getSaldoPendiente().doubleValue()); row.getCell(6).setCellStyle(moneyStyle);

                // NUEVO: Días otorgados
                row.createCell(7).setCellValue(obtenerTextoPlazo(c.getFechaCompra().toLocalDate(), c.getFechaVencimiento())); row.getCell(7).setCellStyle(normalStyle);

                row.createCell(8).setCellValue(c.getEstadoPago().name()); row.getCell(8).setCellStyle(normalStyle);
                row.createCell(9).setCellValue(calcularAlertaVencimiento(c)); row.getCell(9).setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    public ByteArrayInputStream exportarHistorialPagosProveedores(List<PagoProveedor> pagos, String logoUrl, LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pagos a Proveedores");
            CreationHelper helper = workbook.getCreationHelper();

            // Estilos básicos
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            // Nuevo estilo para el bloque de información de la factura
            CellStyle infoLabelStyle = crearEstiloInfoEtiqueta(workbook);
            CellStyle infoValueStyle = crearEstiloInfoValor(workbook);
            CellStyle infoMoneyStyle = crearEstiloInfoMoneda(workbook, helper);

            insertarEncabezadoConLogo(workbook, sheet, "HISTORIAL DE PAGOS A PROVEEDORES", logoUrl, inicio, fin);

            // Ordenar por Comprobante y luego por fecha del pago
            pagos.sort(Comparator.comparing((PagoProveedor p) -> p.getCompra().getComprobante())
                    .thenComparing(PagoProveedor::getFechaPago));

            int rowIdx = 5;
            String ultimoComprobante = "";

            for (PagoProveedor p : pagos) {
                Compra c = p.getCompra();

                // Si detectamos una nueva factura, creamos su "Bloque de Cabecera"
                if (!c.getComprobante().equals(ultimoComprobante)) {

                    // Dejar un espacio si no es la primera factura
                    if (!ultimoComprobante.isEmpty()) {
                        rowIdx++;
                    }

                    // --- FILA 1 DEL BLOQUE: Datos Generales y Alerta ---
                    Row infoRow1 = sheet.createRow(rowIdx++);

                    Cell ir1c0 = infoRow1.createCell(0); ir1c0.setCellValue("FACTURA:"); ir1c0.setCellStyle(infoLabelStyle);
                    Cell ir1c1 = infoRow1.createCell(1); ir1c1.setCellValue(c.getComprobante()); ir1c1.setCellStyle(infoValueStyle);

                    Cell ir1c2 = infoRow1.createCell(2); ir1c2.setCellValue("PROVEEDOR:"); ir1c2.setCellStyle(infoLabelStyle);
                    Cell ir1c3 = infoRow1.createCell(3); ir1c3.setCellValue(c.getProveedor().getRazonSocial()); ir1c3.setCellStyle(infoValueStyle);

                    Cell ir1c4 = infoRow1.createCell(4); ir1c4.setCellValue("ALERTA:"); ir1c4.setCellStyle(infoLabelStyle);
                    Cell ir1c5 = infoRow1.createCell(5); ir1c5.setCellValue(calcularAlertaVencimiento(c)); ir1c5.setCellStyle(infoValueStyle);

                    // --- FILA 2 DEL BLOQUE: Saldos ---
                    Row infoRow2 = sheet.createRow(rowIdx++);

                    Cell ir2c0 = infoRow2.createCell(0); ir2c0.setCellValue("Total Facturado:"); ir2c0.setCellStyle(infoLabelStyle);
                    Cell ir2c1 = infoRow2.createCell(1); ir2c1.setCellValue(c.getTotal().doubleValue()); ir2c1.setCellStyle(infoMoneyStyle);

                    Cell ir2c2 = infoRow2.createCell(2); ir2c2.setCellValue("Saldo Restante:"); ir2c2.setCellStyle(infoLabelStyle);
                    Cell ir2c3 = infoRow2.createCell(3); ir2c3.setCellValue(c.getSaldoPendiente().doubleValue()); ir2c3.setCellStyle(infoMoneyStyle);

                    // NUEVO: En lugar de celdas vacías, mostramos el Plazo
                    Cell ir2c4 = infoRow2.createCell(4); ir2c4.setCellValue("PLAZO:"); ir2c4.setCellStyle(infoLabelStyle);
                    Cell ir2c5 = infoRow2.createCell(5); ir2c5.setCellValue(obtenerTextoPlazo(c.getFechaCompra().toLocalDate(), c.getFechaVencimiento())); ir2c5.setCellStyle(infoValueStyle);

                    // --- FILA 3 DEL BLOQUE: Cabeceras de los Pagos ---
                    Row subHeaderRow = sheet.createRow(rowIdx++);
                    String[] columnas = {"ID Pago", "Fecha Pago", "Método", "Referencia", "Monto Abonado", "Registrado por"};
                    for (int i = 0; i < columnas.length; i++) {
                        Cell cell = subHeaderRow.createCell(i);
                        cell.setCellValue(columnas[i]);
                        cell.setCellStyle(headerStyle);
                    }

                    ultimoComprobante = c.getComprobante();
                }

                // --- FILAS DE DATOS: Los pagos individuales de esa factura ---
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId()); row.getCell(0).setCellStyle(normalStyle);
                row.createCell(1).setCellValue(p.getFechaPago()); row.getCell(1).setCellStyle(dateStyle);
                row.createCell(2).setCellValue(p.getMetodoPago()); row.getCell(2).setCellStyle(normalStyle);
                row.createCell(3).setCellValue(p.getReferencia() != null ? p.getReferencia() : ""); row.getCell(3).setCellStyle(normalStyle);
                row.createCell(4).setCellValue(p.getMonto().doubleValue()); row.getCell(4).setCellStyle(moneyStyle);
                row.createCell(5).setCellValue(p.getUsuario().getNombreCompleto()); row.getCell(5).setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, 6); // Ajustamos las 6 columnas que usamos
            return escribirLibro(workbook);
        }
    }

    private CellStyle crearEstiloInfoEtiqueta(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        // Fondo gris claro para las etiquetas (Ej: "FACTURA:", "SALDO:")
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloInfoValor(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        // Fondo azul muy pálido para los valores
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloInfoMoneda(Workbook workbook, CreationHelper helper) {
        CellStyle style = crearEstiloInfoValor(workbook);
        style.setDataFormat(helper.createDataFormat().getFormat("\"S/\" #,##0.00"));
        return style;
    }
    private void insertarEncabezadoConLogo(Workbook workbook, Sheet sheet, String titleText, String logoUrl, LocalDate inicio, LocalDate fin) {
        Row titleRow = sheet.createRow(2);
        Cell titleCell = titleRow.createCell(2);
        titleCell.setCellValue(titleText);

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        if (inicio != null && fin != null) {
            Row dateRow = sheet.createRow(3);
            Cell dateCell = dateRow.createCell(2);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateCell.setCellValue("Rango consultado: " + inicio.format(formatter) + " al " + fin.format(formatter));

            CellStyle dateStrStyle = workbook.createCellStyle();
            Font dateFont = workbook.createFont();
            dateFont.setItalic(true);
            dateFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            dateStrStyle.setFont(dateFont);
            dateCell.setCellStyle(dateStrStyle);
        }

        InputStream is = null;
        int pictureType = Workbook.PICTURE_TYPE_PNG; // Por defecto

        try {
            if (logoUrl != null && !logoUrl.isBlank() && logoUrl.contains("/uploads/")) {
                String rutaRelativa = "uploads/" + logoUrl.split("/uploads/")[1];
                Path path = Paths.get(rutaRelativa);

                if (Files.exists(path)) {
                    is = Files.newInputStream(path);
                    if (logoUrl.toLowerCase().endsWith(".jpg") || logoUrl.toLowerCase().endsWith(".jpeg")) {
                        pictureType = Workbook.PICTURE_TYPE_JPEG;
                    }
                }
            }

            if (is == null) {
                is = getClass().getResourceAsStream("/logo.png");
                pictureType = Workbook.PICTURE_TYPE_PNG;
            }

            if (is != null) {
                byte[] bytes = IOUtils.toByteArray(is);
                int pictureIdx = workbook.addPicture(bytes, pictureType);
                is.close();

                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();

                anchor.setCol1(0); // Columna A
                anchor.setRow1(0); // Fila 1
                anchor.setCol2(2); // Hasta Columna C
                anchor.setRow2(4); // Hasta Fila 5
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

                drawing.createPicture(anchor, pictureIdx);
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR al procesar el logo en Excel: " + e.getMessage());
        }
    }

    private CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void autoAjustarColumnas(Sheet sheet, int numColumnas) {
        for (int i = 0; i < numColumnas; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    private String obtenerCondicionYPlazo(Venta v) {
        if (v.getFechaVencimiento() == null) {
            return "Contado";
        }

        // Convertimos la fecha de venta (asumiendo que es LocalDateTime) a LocalDate para poder comparar
        LocalDate fechaVenta = v.getFechaVenta().toLocalDate();
        LocalDate fechaVencimiento = v.getFechaVencimiento();

        if (fechaVencimiento.isEqual(fechaVenta) || fechaVencimiento.isBefore(fechaVenta)) {
            return "Contado";
        }

        // Calculamos el plazo otorgado
        long diasOtorgados = java.time.temporal.ChronoUnit.DAYS.between(fechaVenta, fechaVencimiento);

        // Verificamos si ya está pagado para no mostrar alerta de días faltantes
        if (v.getSaldoPendiente() != null && v.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "Crédito (" + diasOtorgados + " días) - Pagado";
        }

        // Calculamos los días restantes respecto a HOY
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);

        if (diasRestantes < 0) {
            return "Crédito (" + diasOtorgados + " días) - Vencido " + Math.abs(diasRestantes) + " días";
        } else if (diasRestantes == 0) {
            return "Crédito (" + diasOtorgados + " días) - Vence hoy";
        } else {
            return "Crédito (" + diasOtorgados + " días) - Faltan " + diasRestantes;
        }
    }
    private ByteArrayInputStream escribirLibro(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private CellStyle crearEstiloFecha(Workbook workbook, CreationHelper helper) {
        CellStyle style = crearEstiloBordes(workbook);
        style.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook, CreationHelper helper) {
        CellStyle style = crearEstiloBordes(workbook);
        style.setDataFormat(helper.createDataFormat().getFormat("\"S/\" #,##0.00"));
        return style;
    }
    private String obtenerTextoPlazo(LocalDate emision, LocalDate vencimiento) {
        if (vencimiento == null) {
            return "Contado";
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(emision, vencimiento);
        return dias > 0 ? dias + " días" : "Contado";
    }
    private CellStyle crearEstiloBordes(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    private CellStyle crearEstiloGrupo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        // Color gris oscuro para la fila de separación
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }


    private String calcularAlertaVencimientoVenta(Venta venta) {
        if (venta.getFechaVencimiento() == null) return "Sin fecha límite";
        if (venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) == 0) return "✅ PAGADA";

        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), venta.getFechaVencimiento());
        if (diasRestantes < 0) return "⚠️ VENCIDO hace " + Math.abs(diasRestantes) + " días";
        else if (diasRestantes == 0) return "⏳ VENCE HOY";
        else return "⏳ Faltan " + diasRestantes + " días";
    }

    private String calcularAlertaVencimiento(Compra compra) {
        if (compra.getFechaVencimiento() == null) return "Sin fecha límite";
        if (compra.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) == 0) return "✅ PAGADA";

        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), compra.getFechaVencimiento());
        if (diasRestantes < 0) return "⚠️ VENCIDO hace " + Math.abs(diasRestantes) + " días";
        else if (diasRestantes == 0) return "⏳ VENCE HOY";
        else return "⏳ Faltan " + diasRestantes + " días";
    }
}