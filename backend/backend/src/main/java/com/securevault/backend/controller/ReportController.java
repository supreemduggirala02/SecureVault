package com.securevault.backend.controller;

import com.securevault.backend.entity.AuditLog;
import com.securevault.backend.service.ReportExportService;
import com.securevault.backend.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    public ReportController(
            ReportService reportService,
            ReportExportService reportExportService) {

        this.reportService = reportService;
        this.reportExportService = reportExportService;
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> getSystemReport() {

        return ResponseEntity.ok(
                reportService.getSystemReport()
        );
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> getAuditReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusDays(30);

        LocalDate end = endDate != null
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        List<AuditLog> logs =
                reportService.getAuditLogs(start, end);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/my-security")
    public ResponseEntity<?> getMySecurityReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusDays(30);

        LocalDate end = endDate != null
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        String email = authentication.getName();

        return ResponseEntity.ok(
                reportService.getUserSecurityReport(
                        email,
                        start,
                        end
                )
        );
    }

    @GetMapping("/my-security/logs")
    public ResponseEntity<?> getMySecurityLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusDays(30);

        LocalDate end = endDate != null
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        String email = authentication.getName();

        List<AuditLog> logs =
                reportService.getUserAuditLogs(
                        email,
                        start,
                        end
                );

        return ResponseEntity.ok(logs);
    }

    // =========================
    // CSV EXPORT
    // =========================

    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusDays(30);

        LocalDate end = endDate != null
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        List<AuditLog> logs =
                reportService.getAuditLogs(start, end);

        byte[] csv =
                reportExportService.generateCsv(logs);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=securevault-audit-report.csv"
                )
                .contentType(
                        MediaType.parseMediaType("text/csv")
                )
                .body(csv);
    }

    // =========================
    // PDF EXPORT
    // =========================

    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null
                ? LocalDate.parse(startDate)
                : LocalDate.now().minusDays(30);

        LocalDate end = endDate != null
                ? LocalDate.parse(endDate)
                : LocalDate.now();

        List<AuditLog> logs =
                reportService.getAuditLogs(start, end);

        byte[] pdf =
                reportExportService.generatePdf(
                        logs,
                        start,
                        end
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=securevault-audit-report.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}