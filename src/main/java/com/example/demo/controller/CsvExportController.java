package com.example.demo.controller;

import com.example.demo.service.CsvExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/export/csv")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class CsvExportController {

    @Autowired
    private CsvExportService csvExportService;

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @GetMapping("/students")
    public ResponseEntity<byte[]> exportStudents() {
        String csvContent = csvExportService.exportStudentsCsv();
        String filename = "students_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    @GetMapping("/classes")
    public ResponseEntity<byte[]> exportClasses() {
        String csvContent = csvExportService.exportClassesCsv();
        String filename = "classes_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    @GetMapping("/attendance")
    public ResponseEntity<byte[]> exportAttendance(@RequestParam(required = false) Long classId) {
        String csvContent = csvExportService.exportAttendanceCsv(classId);
        String filename = "attendance_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    @GetMapping("/test-scores")
    public ResponseEntity<byte[]> exportTestScores(@RequestParam(required = false) Long testId) {
        String csvContent = csvExportService.exportTestScoresCsv(testId);
        String filename = "test_scores_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    @GetMapping("/attendance-summary")
    public ResponseEntity<byte[]> exportAttendanceSummary() {
        String csvContent = csvExportService.exportStudentAttendanceSummary();
        String filename = "attendance_summary_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    @GetMapping("/test-results-summary")
    public ResponseEntity<byte[]> exportTestResultsSummary() {
        String csvContent = csvExportService.exportTestResultsSummary();
        String filename = "test_results_summary_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".csv";
        
        return createCsvResponse(csvContent, filename);
    }

    private ResponseEntity<byte[]> createCsvResponse(String csvContent, String filename) {
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        
        // BOM（Byte Order Mark）を追加してExcelで正しく表示されるようにする
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvWithBom = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, csvWithBom, 0, bom.length);
        System.arraycopy(csvBytes, 0, csvWithBom, bom.length, csvBytes.length);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(csvWithBom.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvWithBom);
    }
}