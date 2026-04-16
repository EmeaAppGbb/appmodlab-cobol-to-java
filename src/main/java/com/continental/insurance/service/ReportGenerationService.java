package com.continental.insurance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Report generation service — translated from COBOL RPTGEN.cbl.
 * <p>
 * Produces a formatted claims processing summary report with counts,
 * percentages, and total payment amounts.
 */
@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String SEPARATOR = repeatChar('=', 80);
    private static final String COMPANY_NAME = "CONTINENTAL INSURANCE GROUP";
    private static final String REPORT_TITLE = "CLAIMS PROCESSING SUMMARY REPORT";

    /**
     * Summary report data holder.
     */
    public static class SummaryReport {
        private final int totalClaims;
        private final int approvedClaims;
        private final int deniedClaims;
        private final int pendingClaims;
        private final BigDecimal totalPaid;
        private final int approvedPercent;
        private final int deniedPercent;
        private final int pendingPercent;
        private final String reportDate;
        private final List<String> reportLines;

        public SummaryReport(int totalClaims, int approvedClaims, int deniedClaims,
                             int pendingClaims, BigDecimal totalPaid, int approvedPercent,
                             int deniedPercent, int pendingPercent, String reportDate,
                             List<String> reportLines) {
            this.totalClaims = totalClaims;
            this.approvedClaims = approvedClaims;
            this.deniedClaims = deniedClaims;
            this.pendingClaims = pendingClaims;
            this.totalPaid = totalPaid;
            this.approvedPercent = approvedPercent;
            this.deniedPercent = deniedPercent;
            this.pendingPercent = pendingPercent;
            this.reportDate = reportDate;
            this.reportLines = reportLines;
        }

        public int getTotalClaims() { return totalClaims; }
        public int getApprovedClaims() { return approvedClaims; }
        public int getDeniedClaims() { return deniedClaims; }
        public int getPendingClaims() { return pendingClaims; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public int getApprovedPercent() { return approvedPercent; }
        public int getDeniedPercent() { return deniedPercent; }
        public int getPendingPercent() { return pendingPercent; }
        public String getReportDate() { return reportDate; }
        public List<String> getReportLines() { return reportLines; }
    }

    /**
     * Generate a summary report from processing statistics.
     * Translates COBOL 0000-MAIN-REPORT through 3000-WRITE-REPORT.
     *
     * @param totalClaims    total number of claims processed
     * @param approvedClaims number of approved claims
     * @param deniedClaims   number of denied claims
     * @param pendingClaims  number of claims pending review
     * @param totalPaid      total payment amount authorized
     * @return a SummaryReport containing calculated percentages and formatted lines
     */
    public SummaryReport generateSummary(int totalClaims, int approvedClaims,
                                         int deniedClaims, int pendingClaims,
                                         BigDecimal totalPaid) {
        log.info("Generating summary report...");

        // 1000-CALCULATE-PERCENTAGES
        int approvedPct = 0;
        int deniedPct = 0;
        int pendingPct = 0;

        if (totalClaims > 0) {
            approvedPct = BigDecimal.valueOf(approvedClaims)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalClaims), 0, RoundingMode.DOWN)
                    .intValue();
            deniedPct = BigDecimal.valueOf(deniedClaims)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalClaims), 0, RoundingMode.DOWN)
                    .intValue();
            pendingPct = BigDecimal.valueOf(pendingClaims)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalClaims), 0, RoundingMode.DOWN)
                    .intValue();
        }

        // 2000-FORMAT-REPORT
        String reportDate = LocalDate.now().format(DATE_FORMAT);

        // 3000-WRITE-REPORT
        List<String> lines = new ArrayList<>();
        lines.add(centerText(COMPANY_NAME, 80));
        lines.add(centerText(REPORT_TITLE, 80));
        lines.add(String.format("PAGE:   1%40sDATE: %s", "", reportDate));
        lines.add(SEPARATOR);
        lines.add("");
        lines.add(String.format("TOTAL CLAIMS PROCESSED:         %,7d", totalClaims));
        lines.add("");
        lines.add(String.format("CLAIMS APPROVED:                %,7d          (%3d%%)", approvedClaims, approvedPct));
        lines.add(String.format("CLAIMS DENIED:                  %,7d          (%3d%%)", deniedClaims, deniedPct));
        lines.add(String.format("CLAIMS PENDING:                 %,7d          (%3d%%)", pendingClaims, pendingPct));
        lines.add("");
        lines.add(String.format("TOTAL PAYMENTS:                $%,14.2f", totalPaid));
        lines.add("");
        lines.add(SEPARATOR);

        for (String line : lines) {
            log.info(line);
        }

        return new SummaryReport(totalClaims, approvedClaims, deniedClaims,
                pendingClaims, totalPaid, approvedPct, deniedPct, pendingPct,
                reportDate, lines);
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(' ');
        }
        sb.append(text);
        return sb.toString();
    }

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
