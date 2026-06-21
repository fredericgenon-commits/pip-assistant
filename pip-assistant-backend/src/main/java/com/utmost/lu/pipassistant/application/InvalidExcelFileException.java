package com.utmost.lu.pipassistant.application;

/** Raised when an uploaded file cannot be read as a valid .xlsx workbook. */
public class InvalidExcelFileException extends RuntimeException {

    public InvalidExcelFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidExcelFileException(String message) {
        super(message);
    }
}
