package com.example.jxc.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;

/**
 * CSV export utility using IO streams (key exam requirement).
 */
public class CsvExportUtil {

    private CsvExportUtil() {
    }

    /**
     * Export data as CSV file to HttpServletResponse.
     *
     * @param response HTTP response
     * @param fileName file name without extension
     * @param headers  column headers
     * @param rows     data rows, each row is a String array
     */
    public static void export(HttpServletResponse response, String fileName,
                              String[] headers, List<String[]> rows) throws IOException {
        // 1. Set response content type
        response.setContentType("text/csv; charset=UTF-8");
        // 2. Set Content-Disposition header for download
        response.setHeader("Content-Disposition",
                "attachment; filename=" + fileName + ".csv");

        OutputStream outputStream = response.getOutputStream();

        // 3. Write BOM bytes for Excel Chinese compatibility
        outputStream.write(0xEF);
        outputStream.write(0xBB);
        outputStream.write(0xBF);

        // 4. Create BufferedWriter wrapping OutputStreamWriter wrapping output stream
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // 5. Write headers joined by comma
            StringJoiner headerJoiner = new StringJoiner(",");
            for (String header : headers) {
                headerJoiner.add(header);
            }
            writer.write(headerJoiner.toString());
            writer.newLine();

            // 6. Write each row joined by comma
            for (String[] row : rows) {
                StringJoiner rowJoiner = new StringJoiner(",");
                for (String cell : row) {
                    rowJoiner.add(cell != null ? cell : "");
                }
                writer.write(rowJoiner.toString());
                writer.newLine();
            }

            // 7. Flush
            writer.flush();
        }
    }
}
