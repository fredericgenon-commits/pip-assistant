package com.utmost.lu.pipassistant.domain.port;

import java.io.InputStream;
import java.util.List;

import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;

/**
 * Outbound port that turns a PM Excel file into the REQ rows it contains, in file order.
 * Non-REQ lines (headers, blanks, comments) are skipped; rows missing mandatory fields are
 * still returned with {@code missingData = true}. Implementations throw
 * {@code InvalidExcelFileException} when the stream is not a readable .xlsx workbook.
 */
public interface ExcelRequirementParser {

    List<ParsedRequirement> parse(InputStream content);
}
