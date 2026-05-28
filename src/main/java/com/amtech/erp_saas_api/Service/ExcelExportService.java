package com.amtech.erp_saas_api.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;

import com.amtech.erp_saas_api.Entity.Venta;

@Service
public class ExcelExportService {

    public ByteArrayInputStream exportarVentas(List<Venta> ventas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registro de Ventas");
            
            // Estilo para cabeceras
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Crear cabecera
            Row header = sheet.createRow(0);
            String[] columnas = {"ID", "Fecha", "Comprobante", "Cliente", "Total", "Estado"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowIdx = 1;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getFechaVenta().toString());
                row.createCell(2).setCellValue(v.getComprobante());
                row.createCell(3).setCellValue(v.getCliente() != null ? v.getCliente().getNombreCompleto() : "N/A");
                row.createCell(4).setCellValue(v.getTotal().doubleValue());
                row.createCell(5).setCellValue(v.getEstado().name());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}