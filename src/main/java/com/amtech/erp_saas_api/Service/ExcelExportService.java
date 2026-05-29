package com.amtech.erp_saas_api.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

import com.amtech.erp_saas_api.Entity.*;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExcelExportService {

    // --- 1. EXPORTAR VENTAS ---
    public ByteArrayInputStream exportarVentas(List<Venta> ventas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registro de Ventas");
            CreationHelper helper = workbook.getCreationHelper();

            // Estilos
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "REGISTRO DE VENTAS");

            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Fecha", "Comprobante", "Tipo", "Cliente", "Subtotal", "IGV", "Total", "Cajero", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(v.getId()); c0.setCellStyle(normalStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(v.getFechaVenta()); c1.setCellStyle(dateStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(v.getComprobante()); c2.setCellStyle(normalStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(v.getTipoComprobante().name()); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General"); c4.setCellStyle(normalStyle);

                Cell c5 = row.createCell(5); c5.setCellValue(v.getSubtotalSinImpuesto().doubleValue()); c5.setCellStyle(moneyStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(v.getImpuestoTotal().doubleValue()); c6.setCellStyle(moneyStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(v.getTotal().doubleValue()); c7.setCellStyle(moneyStyle);

                Cell c8 = row.createCell(8); c8.setCellValue(v.getUsuario().getNombreCompleto()); c8.setCellStyle(normalStyle);
                Cell c9 = row.createCell(9); c9.setCellValue(v.getEstado().name()); c9.setCellStyle(normalStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    // --- 2. EXPORTAR KARDEX ---
    public ByteArrayInputStream exportarKardex(List<Kardex> movimientos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Kardex de Movimientos");
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            CellStyle numberStyle = crearEstiloBordes(workbook);
            numberStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0.00"));

            insertarEncabezadoConLogo(workbook, sheet, "AUDITORÍA DE KARDEX");

            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Fecha", "Tipo", "Motivo", "Producto", "Lote", "Cantidad", "Saldo Físico"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Kardex k : movimientos) {
                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(k.getId()); c0.setCellStyle(normalStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(k.getFechaMovimiento()); c1.setCellStyle(dateStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(k.getTipoMovimiento().name()); c2.setCellStyle(normalStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(k.getMotivo()); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(k.getLote().getProducto().getNombre()); c4.setCellStyle(normalStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(k.getLote().getCodigoLote()); c5.setCellStyle(normalStyle);

                Cell c6 = row.createCell(6); c6.setCellValue(k.getCantidad().doubleValue()); c6.setCellStyle(numberStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(k.getSaldoLote().doubleValue()); c7.setCellStyle(numberStyle);
            }
            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    // --- UTILS INTERNOS ---
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

    private ByteArrayInputStream escribirLibro(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream exportarProveedores(List<Proveedor> proveedores) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registro de Proveedores");
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, "REGISTRO DE PROVEEDORES");

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
    public ByteArrayInputStream exportarDeudasPendientes(List<Venta> deudas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cuentas por Cobrar");

            String title="REPORTE HISTÓRICO DE PAGOS";

            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet,title);

            Row header = sheet.createRow(5);
            String[] columnas = {"Comprobante", "Fecha Emisión", "Vencimiento", "Cliente", "Documento", "Total Facturado", "Saldo Pendiente", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Venta v : deudas) {
                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(v.getComprobante()); c0.setCellStyle(normalStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(v.getFechaVenta());
                c1.setCellStyle(dateStyle);

                Cell c2 = row.createCell(2);
                if (v.getFechaVencimiento() != null) {
                    c2.setCellValue(java.sql.Date.valueOf(v.getFechaVencimiento()));
                    c2.setCellStyle(dateStyle);
                } else {
                    c2.setCellValue("N/A");
                    c2.setCellStyle(normalStyle);
                }

                Cell c3 = row.createCell(3); c3.setCellValue(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "Público General"); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(v.getCliente() != null ? v.getCliente().getDocumentoIdentidad() : "N/A"); c4.setCellStyle(normalStyle);

                Cell c5 = row.createCell(5); c5.setCellValue(v.getTotal().doubleValue()); c5.setCellStyle(moneyStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(v.getSaldoPendiente().doubleValue()); c6.setCellStyle(moneyStyle);

                Cell c7 = row.createCell(7); c7.setCellValue(v.getEstadoPago().name()); c7.setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    public ByteArrayInputStream exportarClientes(List<Cliente> clientes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Directorio de Clientes");
            CreationHelper helper = workbook.getCreationHelper();

            // Estilos
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            // Título y Logo
            String title = "DIRECTORIO DE CLIENTES";
            insertarEncabezadoConLogo(workbook, sheet, title);

            // Cabeceras en fila 5
            Row header = sheet.createRow(5);
            String[] columnas = {"ID", "Documento", "Nombre/Razón Social", "Teléfono", "Email", "Dirección", "Límite de Crédito"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 6;
            for (Cliente c : clientes) {
                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(c.getId()); c0.setCellStyle(normalStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(c.getDocumentoIdentidad()); c1.setCellStyle(normalStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(c.getNombreCompleto()); c2.setCellStyle(normalStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(c.getTelefono() != null ? c.getTelefono() : ""); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(c.getEmail() != null ? c.getEmail() : ""); c4.setCellStyle(normalStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(c.getDireccion() != null ? c.getDireccion() : ""); c5.setCellStyle(normalStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(c.getLimiteCredito() != null ? c.getLimiteCredito().doubleValue() : 0.0);
                c6.setCellStyle(moneyStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    public ByteArrayInputStream exportarHistorialAbonos(List<AbonoVenta> abonos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historial de Pagos");

            String title="REPORTE HISTÓRICO DE ABONOS";

            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle groupStyle = crearEstiloGrupo(workbook);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, title);

            Row header = sheet.createRow(5);
            String[] columnas = {"ID Pago", "Fecha Pago", "Comprobante", "Cliente", "Método", "Referencia", "Monto Abonado", "Cajero"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            abonos.sort(Comparator.comparing((AbonoVenta a) -> a.getVenta().getComprobante())
                    .thenComparing(AbonoVenta::getFechaPago).reversed());

            int rowIdx = 6;
            String ultimoComprobante = "";

            for (AbonoVenta a : abonos) {
                String comprobanteActual = a.getVenta().getComprobante();

                if (!comprobanteActual.equals(ultimoComprobante)) {
                    Row groupRow = sheet.createRow(rowIdx++);
                    Cell groupCell = groupRow.createCell(0);

                    String nombreCliente = a.getVenta().getCliente() != null ? a.getVenta().getCliente().getNombreCompleto() : "Público General";
                    groupCell.setCellValue("Pagos del Comprobante: " + comprobanteActual + "  |  Cliente: " + nombreCliente);
                    groupCell.setCellStyle(groupStyle);

                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, columnas.length - 1));

                    ultimoComprobante = comprobanteActual;
                }

                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(a.getId()); c0.setCellStyle(normalStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(a.getFechaPago()); c1.setCellStyle(dateStyle); // Fecha limpia
                Cell c2 = row.createCell(2); c2.setCellValue(a.getVenta().getComprobante()); c2.setCellStyle(normalStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(a.getVenta().getCliente() != null ? a.getVenta().getCliente().getNombreCompleto() : "N/A"); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(a.getMetodoPago()); c4.setCellStyle(normalStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(a.getReferencia() != null ? a.getReferencia() : ""); c5.setCellStyle(normalStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(a.getMonto().doubleValue()); c6.setCellStyle(moneyStyle); // Moneda con S/
                Cell c7 = row.createCell(7); c7.setCellValue(a.getUsuario().getNombreCompleto()); c7.setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    public ByteArrayInputStream exportarCuentasPorPagar(List<Compra> deudas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Cuentas por Pagar");
            String title = "REPORTE DE CUENTAS POR PAGAR (PROVEEDORES)";
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, title);

            Row header = sheet.createRow(5);
            String[] columnas = {"Comprobante Compra", "Fecha Emisión", "Vencimiento", "Proveedor", "RUC/DNI", "Total Facturado", "Saldo Pendiente", "Estado Pago"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 6;
            for (Compra c : deudas) {
                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(c.getComprobante()); c0.setCellStyle(normalStyle);

                Cell c1 = row.createCell(1); c1.setCellValue(c.getFechaCompra()); c1.setCellStyle(dateStyle);

                Cell c2 = row.createCell(2);
                if (c.getFechaVencimiento() != null) {
                    c2.setCellValue(java.sql.Date.valueOf(c.getFechaVencimiento()));
                    c2.setCellStyle(dateStyle);
                } else {
                    c2.setCellValue("N/A");
                    c2.setCellStyle(normalStyle);
                }

                Cell c3 = row.createCell(3); c3.setCellValue(c.getProveedor().getRazonSocial()); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(c.getProveedor().getDocumentoIdentidad()); c4.setCellStyle(normalStyle);

                Cell c5 = row.createCell(5); c5.setCellValue(c.getTotal().doubleValue()); c5.setCellStyle(moneyStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(c.getSaldoPendiente().doubleValue()); c6.setCellStyle(moneyStyle);

                Cell c7 = row.createCell(7); c7.setCellValue(c.getEstadoPago().name()); c7.setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }

    // =========================================================
    // --- NUEVO: EXPORTAR HISTORIAL DE PAGOS A PROVEEDORES ---
    // =========================================================
    public ByteArrayInputStream exportarHistorialPagosProveedores(List<PagoProveedor> pagos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pagos a Proveedores");
            String title = "HISTORIAL DE PAGOS A PROVEEDORES";
            CreationHelper helper = workbook.getCreationHelper();

            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook, helper);
            CellStyle moneyStyle = crearEstiloMoneda(workbook, helper);
            CellStyle groupStyle = crearEstiloGrupo(workbook);
            CellStyle normalStyle = crearEstiloBordes(workbook);

            insertarEncabezadoConLogo(workbook, sheet, title);

            Row header = sheet.createRow(5);
            String[] columnas = {"ID Pago", "Fecha Pago", "Factura Origen", "Proveedor", "Método", "Referencia", "Monto Pagado", "Registrado por"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ordenar por Comprobante y luego por fecha
            pagos.sort(Comparator.comparing((PagoProveedor p) -> p.getCompra().getComprobante())
                    .thenComparing(PagoProveedor::getFechaPago).reversed());

            int rowIdx = 6;
            String ultimoComprobante = "";

            for (PagoProveedor p : pagos) {
                String comprobanteActual = p.getCompra().getComprobante();

                if (!comprobanteActual.equals(ultimoComprobante)) {
                    Row groupRow = sheet.createRow(rowIdx++);
                    Cell groupCell = groupRow.createCell(0);

                    groupCell.setCellValue("Pagos de Factura: " + comprobanteActual + "  |  Proveedor: " + p.getCompra().getProveedor().getRazonSocial());
                    groupCell.setCellStyle(groupStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, columnas.length - 1));

                    ultimoComprobante = comprobanteActual;
                }

                Row row = sheet.createRow(rowIdx++);

                Cell c0 = row.createCell(0); c0.setCellValue(p.getId()); c0.setCellStyle(normalStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(p.getFechaPago()); c1.setCellStyle(dateStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(p.getCompra().getComprobante()); c2.setCellStyle(normalStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(p.getCompra().getProveedor().getRazonSocial()); c3.setCellStyle(normalStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(p.getMetodoPago()); c4.setCellStyle(normalStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(p.getReferencia() != null ? p.getReferencia() : ""); c5.setCellStyle(normalStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(p.getMonto().doubleValue()); c6.setCellStyle(moneyStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(p.getUsuario().getNombreCompleto()); c7.setCellStyle(normalStyle);
            }

            autoAjustarColumnas(sheet, columnas.length);
            return escribirLibro(workbook);
        }
    }
    private void insertarEncabezadoConLogo(Workbook workbook, Sheet sheet, String Title) {
        Row titleRow = sheet.createRow(2);
        Cell titleCell = titleRow.createCell(2);
        titleCell.setCellValue(Title);

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);

        try {
            InputStream is = getClass().getResourceAsStream("/logo.png");
            if (is != null) {
                byte[] bytes = IOUtils.toByteArray(is);
                int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                is.close();

                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();

                // CORRECCIÓN: Definir exactamente desde qué celda hasta qué celda va el logo
                anchor.setCol1(0); // Columna A
                anchor.setRow1(0); // Fila 1
                anchor.setCol2(2); // Hasta Columna C
                anchor.setRow2(4); // Hasta Fila 5

                // Fijar la imagen para que no se deforme al cambiar el tamaño de las celdas
                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

                drawing.createPicture(anchor, pictureIdx);
            } else {
                System.err.println("❌ ERROR: No se encontró el archivo logo.png en src/main/resources/");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR al procesar la imagen del logo: " + e.getMessage());
        }
    }

    private CellStyle crearEstiloFecha(Workbook workbook, CreationHelper helper) {
        CellStyle style = crearEstiloBordes(workbook);
        // Convierte el formato feo a: 29/05/2026 15:01
        style.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook, CreationHelper helper) {
        CellStyle style = crearEstiloBordes(workbook);
        style.setDataFormat(helper.createDataFormat().getFormat("\"S/\" #,##0.00"));
        return style;
    }

    private CellStyle crearEstiloGrupo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        // Color gris oscuro para el separador de cada factura
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloBordes(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

}