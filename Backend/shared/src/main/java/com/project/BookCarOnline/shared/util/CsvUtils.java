package com.project.BookCarOnline.shared.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class CsvUtils {

    private CsvUtils() {
    }

    public static void writeBom(Writer writer) {
        write(writer, "\uFEFF");
    }

    public static void writeRow(Writer writer, Object... cells) {
        for (int index = 0; index < cells.length; index++) {
            if (index > 0) {
                write(writer, ",");
            }
            String value = cells[index] == null ? "" : String.valueOf(cells[index]);
            boolean formula = !value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0;
            if (formula) {
                value = "'" + value;
            }
            boolean quote = formula
                    || value.indexOf(',') >= 0
                    || value.indexOf('"') >= 0
                    || value.indexOf('\r') >= 0
                    || value.indexOf('\n') >= 0;
            if (quote) {
                write(writer, "\"" + value.replace("\"", "\"\"") + "\"");
            } else {
                write(writer, value);
            }
        }
        write(writer, "\r\n");
    }

    private static void write(Writer writer, String value) {
        try {
            writer.write(value);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
