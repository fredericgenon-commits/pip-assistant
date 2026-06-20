package com.utmost.lu.pipassistant.domain.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value object for a PIP name of the form {@code yy_PIP_n} (e.g. {@code 26_PIP_1}).
 * Centralizes every naming rule: format validation, parsing of the year and the
 * sequence number, ordering and computation of the next code.
 */
public final class PipCode implements Comparable<PipCode> {

    public static final String PATTERN_STRING = "^\\d{2}_PIP_\\d+$";
    private static final Pattern PATTERN = Pattern.compile("^(\\d{2})_PIP_(\\d+)$");

    private final int year;      // 2-digit year, e.g. 26
    private final int sequence;  // the n in yy_PIP_n

    private PipCode(int year, int sequence) {
        this.year = year;
        this.sequence = sequence;
    }

    /**
     * Parses and validates a raw code.
     *
     * @throws IllegalArgumentException if {@code raw} does not match {@code yy_PIP_n}.
     */
    public static PipCode of(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("PIP code must not be null");
        }
        Matcher matcher = PATTERN.matcher(raw.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid PIP code '" + raw + "', expected format yy_PIP_n (e.g. 26_PIP_1)");
        }
        int year = Integer.parseInt(matcher.group(1));
        int sequence = Integer.parseInt(matcher.group(2));
        return new PipCode(year, sequence);
    }

    /** First PIP of a given 2-digit year, i.e. {@code yy_PIP_1}. */
    public static PipCode firstForYear(int twoDigitYear) {
        if (twoDigitYear < 0 || twoDigitYear > 99) {
            throw new IllegalArgumentException("Year must be a 2-digit value, got " + twoDigitYear);
        }
        return new PipCode(twoDigitYear, 1);
    }

    /** Same year, next sequence number (used by the "New" button suggestion). */
    public PipCode nextSequence() {
        return new PipCode(year, sequence + 1);
    }

    public int year() {
        return year;
    }

    public int sequence() {
        return sequence;
    }

    public String value() {
        return "%02d_PIP_%d".formatted(year, sequence);
    }

    @Override
    public int compareTo(PipCode other) {
        int byYear = Integer.compare(this.year, other.year);
        return byYear != 0 ? byYear : Integer.compare(this.sequence, other.sequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PipCode other)) {
            return false;
        }
        return year == other.year && sequence == other.sequence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, sequence);
    }

    @Override
    public String toString() {
        return value();
    }
}
