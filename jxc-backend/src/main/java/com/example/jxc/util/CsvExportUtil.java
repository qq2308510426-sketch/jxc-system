package com.example.jxc.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

public class CsvExportUtil {

    private CsvExportUtil() {
    }

    private static String sanitizeCell(String cell) {
        if (cell == null) return "";
        String trimmed = cell.trim();
        if (trimmed.isEmpty()) return "";
        char first = trimmed.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r' || first == '\n') {
            return "'" + trimmed;
        }
        if (trimmed.contains("\"") || trimmed.contains(",") || trimmed.contains("\n") || trimmed.contains("\r")) {
            return "\"" + trimmed.replace("\"", "\"\"") + "\"";
        }
        return trimmed;
    }

    public static void export(HttpServletResponse response, String fileName,
                              String[] headers, List<String[]> rows) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + fileName + ".csv");

        OutputStream outputStream = response.getOutputStream();

        outputStream.write(0xEF);
        outputStream.write(0xBB);
        outputStream.write(0xBF);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            StringJoiner headerJoiner = new StringJoiner(",");
            for (String header : headers) {
                headerJoiner.add(sanitizeCell(header));
            }
            writer.write(headerJoiner.toString());
            writer.newLine();

            for (String[] row : rows) {
                StringJoiner rowJoiner = new StringJoiner(",");
                for (String cell : row) {
                    rowJoiner.add(sanitizeCell(cell));
                }
                writer.write(rowJoiner.toString());
                writer.newLine();
            }

            writer.flush();
        }
    }
}