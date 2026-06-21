package com.utmost.lu.pipassistant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.utmost.lu.pipassistant.application.InvalidExcelFileException;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.infrastructure.config.ExcelImportProperties;

class PoiExcelRequirementParserTest {

    private final PoiExcelRequirementParser parser = new PoiExcelRequirementParser(properties());

    private static ExcelImportProperties properties() {
        ExcelImportProperties props = new ExcelImportProperties();
        props.setSheetIndex(0);
        props.setFirstDataRow(1);
        ExcelImportProperties.Columns cols = props.getColumns();
        cols.setTcm(0);
        cols.setTcmDescription(1);
        cols.setReq(2);
        cols.setReqDescription(3);
        cols.setComment(4);
        Map<String, Integer> teams = new LinkedHashMap<>();
        teams.put("Core", 5);
        teams.put("Portal", 6);
        cols.setTeams(teams);
        return props;
    }

    private static void writeRow(Sheet sheet, int rowIndex, Object... values) {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < values.length; c++) {
            Object v = values[c];
            if (v == null) {
                continue;
            }
            if (v instanceof Number n) {
                row.createCell(c).setCellValue(n.doubleValue());
            } else {
                row.createCell(c).setCellValue(v.toString());
            }
        }
    }

    private static byte[] workbook() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Plan");
            writeRow(sheet, 0, "TCM", "TCM Description", "REQ", "REQ Title", "Comment", "Core", "Portal");
            writeRow(sheet, 1, "TCM-1", "Project A", "REQ-10", "First requirement", "go", 5, null);
            // row 2 left blank (skipped)
            writeRow(sheet, 3, "just a note");                                   // no REQ key -> skipped
            writeRow(sheet, 4, "TCM-2", "Project B", "REQ-11", null, "", null);  // missing REQ description
            writeRow(sheet, 5, null, null, "REQ-12", "Twelfth", "", 3);          // missing TCM key
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void parsesReqLines_skipsNonReq_andFlagsMissingData() throws Exception {
        List<ParsedRequirement> result = parser.parse(new ByteArrayInputStream(workbook()));

        assertThat(result).extracting(ParsedRequirement::reqKey)
                .containsExactly("REQ-10", "REQ-11", "REQ-12");

        ParsedRequirement first = result.get(0);
        assertThat(first.missingData()).isFalse();
        assertThat(first.tcmKey()).isEqualTo("TCM-1");
        assertThat(first.workloadsByTeam()).containsEntry("Core", new java.math.BigDecimal("5.0"));
        assertThat(first.workloadsByTeam()).doesNotContainKey("Portal");

        assertThat(result.get(1).missingData()).isTrue();  // no REQ description
        assertThat(result.get(2).missingData()).isTrue();  // no TCM key
    }

    @Test
    void rejectsNonXlsxContent() {
        assertThatExceptionOfType(InvalidExcelFileException.class).isThrownBy(
                () -> parser.parse(new ByteArrayInputStream("not a workbook".getBytes())));
    }
}
