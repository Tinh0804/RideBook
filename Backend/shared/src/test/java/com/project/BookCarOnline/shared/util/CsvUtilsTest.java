package com.project.BookCarOnline.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class CsvUtilsTest {

    @Test
    void writesRfc4180RowsAndNeutralizesSpreadsheetFormulas() {
        StringWriter output = new StringWriter();

        CsvUtils.writeRow(output, "plain", "comma,value", "line\nbreak", "=SUM(A1:A2)", null);

        assertThat(output.toString())
                .isEqualTo("plain,\"comma,value\",\"line\nbreak\",\"'=SUM(A1:A2)\",\r\n");
    }

    @Test
    void writesUtf8Bom() {
        StringWriter output = new StringWriter();

        CsvUtils.writeBom(output);

        assertThat(output.toString()).isEqualTo("\uFEFF");
    }
}
