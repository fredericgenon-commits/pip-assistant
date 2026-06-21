package com.utmost.lu.pipassistant.infrastructure.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Excel import layout bound from {@code pip.import} in application config. Columns are
 * matched by 0-based position (the file's header labels may differ, e.g. "REQ Title").
 */
@Component
@ConfigurationProperties(prefix = "pip.import")
public class ExcelImportProperties {

    /** 0-based index of the sheet to read. */
    private int sheetIndex = 0;

    /** 0-based index of the first data row (skips the header row(s)). */
    private int firstDataRow = 1;

    private Columns columns = new Columns();

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public int getFirstDataRow() {
        return firstDataRow;
    }

    public void setFirstDataRow(int firstDataRow) {
        this.firstDataRow = firstDataRow;
    }

    public Columns getColumns() {
        return columns;
    }

    public void setColumns(Columns columns) {
        this.columns = columns;
    }

    /** 0-based column indices for each logical field. */
    public static class Columns {
        private int tcm;
        private int tcmDescription;
        private int req;
        private int reqDescription;
        private int comment;
        /** Team name (matching the seeded teams) -> 0-based column index. */
        private Map<String, Integer> teams = new LinkedHashMap<>();

        public int getTcm() {
            return tcm;
        }

        public void setTcm(int tcm) {
            this.tcm = tcm;
        }

        public int getTcmDescription() {
            return tcmDescription;
        }

        public void setTcmDescription(int tcmDescription) {
            this.tcmDescription = tcmDescription;
        }

        public int getReq() {
            return req;
        }

        public void setReq(int req) {
            this.req = req;
        }

        public int getReqDescription() {
            return reqDescription;
        }

        public void setReqDescription(int reqDescription) {
            this.reqDescription = reqDescription;
        }

        public int getComment() {
            return comment;
        }

        public void setComment(int comment) {
            this.comment = comment;
        }

        public Map<String, Integer> getTeams() {
            return teams;
        }

        public void setTeams(Map<String, Integer> teams) {
            this.teams = teams;
        }
    }
}
