package com.utmost.lu.pipassistant.infrastructure.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.application.InvalidExcelFileException;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.port.ExcelRequirementParser;
import com.utmost.lu.pipassistant.infrastructure.config.ExcelImportProperties;

/**
 * Apache POI implementation of {@link ExcelRequirementParser}. Reads the configured sheet,
 * skips lines without a {@code REQ-xxx} key, and flags rows missing a mandatory field
 * (TCM key, TCM/REQ description) as {@code missingData}.
 */
@Component
public class PoiExcelRequirementParser implements ExcelRequirementParser {

    private static final Pattern TCM_PATTERN = Pattern.compile("TCM-\\d+", Pattern.CASE_INSENSITIVE);
    private static final Pattern REQ_PATTERN = Pattern.compile("REQ-\\d+", Pattern.CASE_INSENSITIVE);

    private final ExcelImportProperties properties;

    public PoiExcelRequirementParser(ExcelImportProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<ParsedRequirement> parse(InputStream content) {
        ExcelImportProperties.Columns cols = properties.getColumns();
        DataFormatter formatter = new DataFormatter();
        List<ParsedRequirement> result = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(content)) {
            Sheet sheet = workbook.getSheetAt(properties.getSheetIndex());
            int order = 0;
            for (int r = properties.getFirstDataRow(); r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String reqKey = extract(REQ_PATTERN, text(row, cols.getReq(), formatter));
                if (reqKey == null) {
                    continue; // not a REQ line -> ignore (header, blank, comment, ...)
                }
                String tcmKey = extract(TCM_PATTERN, text(row, cols.getTcm(), formatter));
                String tcmDescription = text(row, cols.getTcmDescription(), formatter);
                String reqDescription = text(row, cols.getReqDescription(), formatter);
                String pmComment = text(row, cols.getComment(), formatter);

                Map<String, BigDecimal> workloads = new LinkedHashMap<>();
                for (Map.Entry<String, Integer> team : cols.getTeams().entrySet()) {
                    BigDecimal estimate = numeric(row, team.getValue());
                    if (estimate != null) {
                        workloads.put(team.getKey(), estimate);
                    }
                }

                boolean missingData = isBlank(tcmKey) || isBlank(tcmDescription) || isBlank(reqDescription);
                result.add(new ParsedRequirement(order++, tcmKey, tcmDescription, reqKey,
                        reqDescription, pmComment, workloads, missingData));
            }
            return result;
        } catch (IOException | RuntimeException e) {
            throw new InvalidExcelFileException("Unable to read the Excel file: " + e.getMessage(), e);
        }
    }

    /** Trimmed display value of a cell, or null when empty. */
    private String text(Row row, int columnIndex, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        return isBlank(value) ? null : value.trim();
    }

    /** Numeric value of a cell (string numbers tolerated), or null when empty / non-numeric. */
    private BigDecimal numeric(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        if (type == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (type == CellType.STRING) {
            String raw = cell.getStringCellValue().trim().replace(',', '.');
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String extract(Pattern pattern, String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group().toUpperCase(Locale.ROOT) : null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
