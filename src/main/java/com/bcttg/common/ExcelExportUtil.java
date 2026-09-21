package com.bcttg.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;

public final class ExcelExportUtil {
    private ExcelExportUtil() {
    }

    public static ByteArrayResource buildWorkbook(String sheetName, List<String> headers, List<List<String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);
            writeHeader(sheet, headers);
            writeRows(sheet, rows);
            autosize(sheet, headers.size());
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Khong tao duoc file xuat", List.of(ex.getMessage()));
        }
    }

    private static void writeHeader(XSSFSheet sheet, List<String> headers) {
        Row row = sheet.createRow(0);
        for (int index = 0; index < headers.size(); index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers.get(index));
        }
    }

    private static void writeRows(XSSFSheet sheet, List<List<String>> rows) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            List<String> values = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < values.size(); columnIndex++) {
                row.createCell(columnIndex).setCellValue(values.get(columnIndex));
            }
        }
    }

    private static void autosize(XSSFSheet sheet, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            sheet.autoSizeColumn(index);
        }
    }
}
