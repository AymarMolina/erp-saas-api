package com.amtech.erp_saas_api.Service;

import com.amtech.erp_saas_api.Entity.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    // ==========================================
    // 1. VENTAS
    // ==========================================
    public ByteArrayInputStream exportarVentasPdf(List<Venta> ventas, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Registro Oficial de Ventas", logoUrl, inicio, fin);

            // 12 columnas en total
            PdfPTable table = new PdfPTable(12);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.5f, 1.8f, 2.2f, 1.5f, 1.8f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.2f});

            // Cabeceras actualizadas con el Subtotal fijo y los IGV separados
            String[] columnas = {"ID", "Fecha", "Comprob", "Cliente", "Doc", "Condición", "Subtotal", "IGV 18%", "IGV 10%", "Inafecto", "Total", "Estado"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Venta v : ventas) {
                table.addCell(crearCelda(String.valueOf(v.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getFechaVenta().toLocalDate().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getComprobante(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General", cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(obtenerCondicionYPlazo(v), cellFont, Element.ALIGN_CENTER));

                double subtotal = v.getSubtotalSinImpuesto().doubleValue();
                double igv = v.getImpuestoTotal().doubleValue();
                long porcentajeIgv = subtotal > 0 ? Math.round((igv / subtotal) * 100) : 0;

                // 1. Subtotal Gravado (vacío si es inafecto)
                String strSubtotal = (porcentajeIgv > 0) ? "S/ " + v.getSubtotalSinImpuesto().toString() : "";
                table.addCell(crearCelda(strSubtotal, cellFont, Element.ALIGN_RIGHT));

                // 2. Repartir el IGV o el Inafecto
                String igv18 = (porcentajeIgv == 18) ? "S/ " + v.getImpuestoTotal().toString() : "";
                String igv10 = (porcentajeIgv == 10) ? "S/ " + v.getImpuestoTotal().toString() : "";
                String inafecto = (porcentajeIgv == 0) ? "S/ " + v.getSubtotalSinImpuesto().toString() : ""; // El monto base va aquí

                table.addCell(crearCelda(igv18, cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(igv10, cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(inafecto, cellFont, Element.ALIGN_RIGHT));
                // -------------------------

                // 3. Columna Fija: Total
                table.addCell(crearCelda("S/ " + v.getTotal().toString(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(v.getEstado().name(), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }
    // ==========================================
    // 9. REGISTRO DE COMPRAS
    // ==========================================
    public ByteArrayInputStream exportarComprasPdf(List<Compra> compras, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Registro Oficial de Compras", logoUrl, inicio, fin);

            // 12 columnas en total para que cuadre perfecto
            PdfPTable table = new PdfPTable(12);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 1.5f, 1.8f, 2.2f, 1.5f, 1.8f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.2f});

            // Cabeceras con la separación de impuestos
            String[] columnas = {"ID", "Fecha", "Comprob", "Proveedor", "Doc", "Condición", "Base Gravada", "IGV 18%", "IGV 10%", "Inafecto", "Total", "Estado"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Compra c : compras) {
                table.addCell(crearCelda(String.valueOf(c.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getFechaCompra().toLocalDate().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getComprobante(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getProveedor().getRazonSocial(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(c.getProveedor().getDocumentoIdentidad(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getCondicionPago().name(), cellFont, Element.ALIGN_CENTER));

                // --- LÓGICA DE DEDUCCIÓN DEL IGV ---
                double subtotal = c.getSubtotalSinImpuesto().doubleValue();
                double igv = c.getImpuestoTotal().doubleValue();
                long porcentajeIgv = subtotal > 0 ? Math.round((igv / subtotal) * 100) : 0;

                // 1. Subtotal Gravado (vacío si es inafecto)
                String strSubtotal = (porcentajeIgv > 0) ? "S/ " + c.getSubtotalSinImpuesto().toString() : "";
                table.addCell(crearCelda(strSubtotal, cellFont, Element.ALIGN_RIGHT));

                // 2. Repartir el IGV o el Inafecto
                String igv18 = (porcentajeIgv == 18) ? "S/ " + c.getImpuestoTotal().toString() : "";
                String igv10 = (porcentajeIgv == 10) ? "S/ " + c.getImpuestoTotal().toString() : "";
                String inafecto = (porcentajeIgv == 0) ? "S/ " + c.getSubtotalSinImpuesto().toString() : "";

                table.addCell(crearCelda(igv18, cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(igv10, cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(inafecto, cellFont, Element.ALIGN_RIGHT));
                // -----------------------------------

                table.addCell(crearCelda("S/ " + c.getTotal().toString(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(c.getEstado().name(), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }
    // ==========================================
    // 2. KARDEX
    // ==========================================
    public ByteArrayInputStream exportarKardexPdf(List<Kardex> movimientos, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Auditoría de Kardex", logoUrl, inicio, fin);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 2f, 2f, 3f, 2f, 1.5f, 1.5f});

            String[] columnas = {"ID", "Fecha", "Tipo", "Motivo", "Producto", "Lote", "Cant.", "Saldo"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Kardex k : movimientos) {
                table.addCell(crearCelda(String.valueOf(k.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(k.getFechaMovimiento().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(k.getTipoMovimiento().name(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(k.getMotivo(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(k.getLote().getProducto().getNombre(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(k.getLote().getCodigoLote(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(k.getCantidad().toString(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(k.getSaldoLote().toString(), cellFont, Element.ALIGN_RIGHT));
            }
            document.add(table);

            // ==============================================================
            // NUEVO: RESUMEN DE STOCK AL FINAL DEL PDF
            // ==============================================================
            if (!movimientos.isEmpty()) {
                document.add(new Paragraph(" ")); // Espacio en blanco

                Font boldFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
                Paragraph title = new Paragraph("RESUMEN DE STOCK ACTUAL (Lotes involucrados)", boldFont);
                title.setSpacingAfter(10f);
                document.add(title);

                // Tabla de resumen de 3 columnas
                PdfPTable summaryTable = new PdfPTable(3);
                summaryTable.setWidthPercentage(50); // Ocupará la mitad de la hoja
                summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                summaryTable.setWidths(new float[]{3f, 2f, 2f});

                agregarCabeceras(summaryTable, new String[]{"Producto", "Lote", "Stock Actual"});

                // Filtrar lotes únicos
                Map<Integer, Kardex> lotesUnicos = new HashMap<>();
                for (Kardex k : movimientos) {
                    if (k.getLote() != null) lotesUnicos.put(k.getLote().getId(), k);
                }

                // Llenar datos y sumar total
                double stockTotal = 0.0;
                for (Kardex k : lotesUnicos.values()) {
                    summaryTable.addCell(crearCelda(k.getLote().getProducto().getNombre(), cellFont, Element.ALIGN_LEFT));
                    summaryTable.addCell(crearCelda(k.getLote().getCodigoLote(), cellFont, Element.ALIGN_CENTER));

                    double cantActual = k.getLote().getCantidadActual().doubleValue();
                    summaryTable.addCell(crearCelda(String.valueOf(cantActual), cellFont, Element.ALIGN_RIGHT));
                    stockTotal += cantActual;
                }

                // Fila final de Total
                PdfPCell emptyCell = new PdfPCell(new Phrase(""));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                summaryTable.addCell(emptyCell);

                PdfPCell totalLabel = crearCelda("TOTAL STOCK:", boldFont, Element.ALIGN_RIGHT);
                totalLabel.setBorder(Rectangle.NO_BORDER);
                summaryTable.addCell(totalLabel);

                summaryTable.addCell(crearCelda(String.valueOf(stockTotal), boldFont, Element.ALIGN_RIGHT));

                document.add(summaryTable);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 3. PROVEEDORES
    // ==========================================
    public ByteArrayInputStream exportarProveedoresPdf(List<Proveedor> proveedores, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4, 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Directorio de Proveedores", logoUrl, inicio, fin);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 2f, 2f, 3f});

            String[] columnas = {"ID", "Razón Social", "RUC/DNI", "Teléfono", "Email"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Proveedor p : proveedores) {
                table.addCell(crearCelda(String.valueOf(p.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getRazonSocial(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getDocumentoIdentidad(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getTelefono() != null ? p.getTelefono() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getEmail() != null ? p.getEmail() : "-", cellFont, Element.ALIGN_LEFT));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 4. CLIENTES
    // ==========================================
    public ByteArrayInputStream exportarClientesPdf(List<Cliente> clientes, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Directorio de Clientes", logoUrl, inicio, fin);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 3f, 2f, 3f, 2f});

            String[] columnas = {"ID", "Documento", "Nombre/Razón Social", "Teléfono", "Email", "Límite Crédito"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Cliente c : clientes) {
                table.addCell(crearCelda(String.valueOf(c.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getDocumentoIdentidad(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getNombreCompleto(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(c.getTelefono() != null ? c.getTelefono() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getEmail() != null ? c.getEmail() : "-", cellFont, Element.ALIGN_LEFT));
                String credito = c.getLimiteCredito() != null ? "S/ " + c.getLimiteCredito() : "S/ 0.00";
                table.addCell(crearCelda(credito, cellFont, Element.ALIGN_RIGHT));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 5. CUENTAS POR COBRAR
    // ==========================================
    public ByteArrayInputStream exportarDeudasPendientesPdf(List<Venta> deudas, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Reporte de Cuentas por Cobrar", logoUrl, inicio, fin);

            // NUEVO: Aumentamos a 9 columnas
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            // Ajustamos los pesos de las columnas para la nueva columna "Alerta"
            table.setWidths(new float[]{1.8f, 1.8f, 1.8f, 3f, 2f, 1.8f, 1.8f, 1.5f, 2.5f});

            // NUEVO: Se añade "Alerta" al final
            String[] columnas = {"Comprobante", "Emisión", "Vencimiento", "Cliente", "Documento", "Total", "Saldo", "Estado", "Alerta"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Venta v : deudas) {
                table.addCell(crearCelda(v.getComprobante(), cellFont, Element.ALIGN_CENTER));

                // Mostramos solo la fecha para ahorrar espacio visual
                table.addCell(crearCelda(v.getFechaVenta().toLocalDate().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(v.getFechaVencimiento() != null ? v.getFechaVencimiento().toString() : "-", cellFont, Element.ALIGN_CENTER));

                table.addCell(crearCelda(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público", cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda("S/ " + v.getTotal(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda("S/ " + v.getSaldoPendiente(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(v.getEstadoPago().name(), cellFont, Element.ALIGN_CENTER));

                // NUEVO: Calculamos la alerta y la añadimos
                table.addCell(crearCelda(calcularAlertaVencimientoVentaSinEmojis(v), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 6. HISTORIAL DE ABONOS
    // ==========================================
    public ByteArrayInputStream exportarHistorialAbonosPdf(List<AbonoVenta> abonos, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Historial de Cobros y Abonos", logoUrl, inicio, fin);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 2f, 3f, 2f, 2f, 2f, 2f});

            String[] columnas = {"ID", "Fecha Pago", "Factura", "Cliente", "Método", "Referencia", "Monto", "Cajero"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (AbonoVenta a : abonos) {
                table.addCell(crearCelda(String.valueOf(a.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getFechaPago().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getVenta().getComprobante(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getVenta().getCliente() != null ? a.getVenta().getCliente().getNombreCompleto() : "-", cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(a.getMetodoPago(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(a.getReferencia() != null ? a.getReferencia() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda("S/ " + a.getMonto(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(a.getUsuario().getNombreCompleto(), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // 7. CUENTAS POR PAGAR
    // ==========================================
    // ==========================================
    // 7. CUENTAS POR PAGAR
    // ==========================================
    public ByteArrayInputStream exportarCuentasPorPagarPdf(List<Compra> deudas, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Reporte de Cuentas por Pagar", logoUrl, inicio, fin);

            // NUEVO: Aumentamos a 9 columnas
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            // Ajustamos los anchos de las columnas
            table.setWidths(new float[]{1.8f, 1.8f, 1.8f, 3f, 2f, 1.8f, 1.8f, 1.5f, 2.5f});

            // NUEVO: Agregamos "Alerta" al final
            String[] columnas = {"Factura", "Emisión", "Vencimiento", "Proveedor", "RUC", "Total", "Saldo", "Estado", "Alerta"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (Compra c : deudas) {
                table.addCell(crearCelda(c.getComprobante(), cellFont, Element.ALIGN_CENTER));

                // Mostramos solo la fecha para ahorrar espacio visual
                table.addCell(crearCelda(c.getFechaCompra().toLocalDate().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(c.getFechaVencimiento() != null ? c.getFechaVencimiento().toString() : "-", cellFont, Element.ALIGN_CENTER));

                table.addCell(crearCelda(c.getProveedor().getRazonSocial(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(c.getProveedor().getDocumentoIdentidad(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda("S/ " + c.getTotal(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda("S/ " + c.getSaldoPendiente(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(c.getEstadoPago().name(), cellFont, Element.ALIGN_CENTER));

                // NUEVO: Calculamos la alerta y la añadimos
                table.addCell(crearCelda(calcularAlertaVencimientoSinEmojis(c), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- AGREGAR ESTE HELPER EN PdfExportService ---
    private String calcularAlertaVencimientoSinEmojis(Compra compra) {
        if (compra.getFechaVencimiento() == null) {
            return "Sin fecha límite";
        }

        LocalDate fechaCompra = compra.getFechaCompra().toLocalDate();
        LocalDate fechaVencimiento = compra.getFechaVencimiento();

        // Calculamos cuántos días de crédito nos dio el proveedor
        long diasOtorgados = java.time.temporal.ChronoUnit.DAYS.between(fechaCompra, fechaVencimiento);
        String textoPlazo = diasOtorgados > 0 ? "(Plazo: " + diasOtorgados + " días)" : "(Contado)";

        if (compra.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "PAGADA " + textoPlazo;
        }

        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);

        if (diasRestantes < 0) {
            return "VENCIDO hace " + Math.abs(diasRestantes) + " días " + textoPlazo;
        } else if (diasRestantes == 0) {
            return "VENCE HOY " + textoPlazo;
        } else {
            return "Faltan " + diasRestantes + " días " + textoPlazo;
        }
    }

    // ==========================================
    // 8. HISTORIAL DE PAGOS A PROVEEDORES
    // ==========================================
    public ByteArrayInputStream exportarHistorialPagosProveedoresPdf(List<PagoProveedor> pagos, String logoUrl, LocalDate inicio, LocalDate fin) {
        Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoConLogoYFechas(document, "Historial de Pagos a Proveedores", logoUrl, inicio, fin);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 2f, 3f, 2f, 2f, 2f, 2f});

            String[] columnas = {"ID", "Fecha Pago", "Factura", "Proveedor", "Método", "Referencia", "Monto", "Registrado por"};
            agregarCabeceras(table, columnas);

            Font cellFont = getCellFont();
            for (PagoProveedor p : pagos) {
                table.addCell(crearCelda(String.valueOf(p.getId()), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getFechaPago().toString(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getCompra().getComprobante(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getCompra().getProveedor().getRazonSocial(), cellFont, Element.ALIGN_LEFT));
                table.addCell(crearCelda(p.getMetodoPago(), cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda(p.getReferencia() != null ? p.getReferencia() : "-", cellFont, Element.ALIGN_CENTER));
                table.addCell(crearCelda("S/ " + p.getMonto(), cellFont, Element.ALIGN_RIGHT));
                table.addCell(crearCelda(p.getUsuario().getNombreCompleto(), cellFont, Element.ALIGN_CENTER));
            }
            document.add(table);
            document.close();
        } catch (Exception e) { e.printStackTrace(); }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==========================================
    // METODOS AUXILIARES Y NUEVO ENCABEZADO
    // ==========================================

    private void agregarEncabezadoConLogoYFechas(Document document, String titulo, String logoUrl, LocalDate inicio, LocalDate fin) throws DocumentException {
        // Creamos una tabla para el encabezado con 2 columnas (Logo a la izquierda, Texto al centro/derecha)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[]{1.5f, 4f}); // Proporción de tamaño (Logo más pequeño)
        } catch (Exception e) { e.printStackTrace(); }

        // --- 1. PROCESAR LOGO ---
        Image img = null;
        try {
            if (logoUrl != null && !logoUrl.isBlank() && logoUrl.contains("/uploads/")) {
                String rutaRelativa = "uploads/" + logoUrl.split("/uploads/")[1];
                Path path = Paths.get(rutaRelativa);
                if (Files.exists(path)) {
                    img = Image.getInstance(path.toAbsolutePath().toString());
                }
            }
            // Si no se encontró en uploads, buscar el logo por defecto en resources
            if (img == null) {
                URL resource = getClass().getResource("/logo.png");
                if (resource != null) {
                    img = Image.getInstance(resource);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR al cargar logo en PDF: " + e.getMessage());
        }

        // Celda del logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (img != null) {
            img.scaleToFit(80, 80); // Ajustar el tamaño del logo
            img.setAlignment(Element.ALIGN_LEFT);
            logoCell.addElement(img);
        }
        headerTable.addCell(logoCell);

        // --- 2. PROCESAR TÍTULO Y FECHAS ---
        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Título
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph pTitulo = new Paragraph(titulo, fontTitle);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(pTitulo);

        // Fechas
        if (inicio != null && fin != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Font fontFechas = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);
            Paragraph pFechas = new Paragraph("Rango consultado: " + inicio.format(formatter) + " al " + fin.format(formatter), fontFechas);
            pFechas.setAlignment(Element.ALIGN_CENTER);
            pFechas.setSpacingBefore(5);
            textCell.addElement(pFechas);
        }

        headerTable.addCell(textCell);

        // Agregar la tabla de encabezado al documento y dar un poco de espacio antes de la tabla de datos
        document.add(headerTable);

        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingAfter(10);
        document.add(espacio);
    }

    private void agregarCabeceras(PdfPTable table, String[] columnas) {
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String col : columnas) {
            PdfPCell hcell = new PdfPCell(new Phrase(col, headFont));
            hcell.setBackgroundColor(new Color(29, 78, 216)); // Azul
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hcell.setPadding(6);
            table.addCell(hcell);
        }
    }

    private String calcularAlertaVencimientoVentaSinEmojis(Venta venta) {
        if (venta.getFechaVencimiento() == null) {
            return "Sin fecha límite";
        }

        LocalDate fechaVenta = venta.getFechaVenta().toLocalDate();
        LocalDate fechaVencimiento = venta.getFechaVencimiento();

        // Calculamos cuántos días de crédito se le dio originalmente
        long diasOtorgados = java.time.temporal.ChronoUnit.DAYS.between(fechaVenta, fechaVencimiento);
        String textoPlazo = diasOtorgados > 0 ? "(Plazo: " + diasOtorgados + " días)" : "(Contado)";

        if (venta.getSaldoPendiente().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "PAGADA " + textoPlazo;
        }

        // Calculamos cuántos días faltan desde HOY
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);

        if (diasRestantes < 0) {
            return "VENCIDO hace " + Math.abs(diasRestantes) + " días " + textoPlazo;
        } else if (diasRestantes == 0) {
            return "VENCE HOY " + textoPlazo;
        } else {
            return "Faltan " + diasRestantes + " días " + textoPlazo;
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
    private Font getCellFont() {
        return FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    }

    private PdfPCell crearCelda(String texto, Font font, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(5);
        return celda;
    }
}