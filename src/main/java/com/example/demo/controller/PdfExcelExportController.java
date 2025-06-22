package com.example.demo.controller;

import com.example.demo.service.ExcelExportService;
import com.example.demo.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/export")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class PdfExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;
    
    @Autowired
    private PdfExportService pdfExportService;

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // Excel エクスポート
    @GetMapping("/excel/student-attendance/{studentId}")
    public ResponseEntity<byte[]> exportStudentAttendanceExcel(@PathVariable Long studentId) throws IOException {
        byte[] excelData = excelExportService.exportStudentAttendanceReport(studentId);
        String filename = "student_attendance_" + studentId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    @GetMapping("/excel/test-results/{testId}")
    public ResponseEntity<byte[]> exportTestResultsExcel(@PathVariable Long testId) throws IOException {
        byte[] excelData = excelExportService.exportTestResultsReport(testId);
        String filename = "test_results_" + testId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    @GetMapping("/excel/class-report/{classId}")
    public ResponseEntity<byte[]> exportClassReportExcel(@PathVariable Long classId) throws IOException {
        byte[] excelData = excelExportService.exportClassReport(classId);
        String filename = "class_report_" + classId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";
        
        return createExcelResponse(excelData, filename);
    }

    // PDF エクスポート
    @GetMapping("/pdf/student-attendance/{studentId}")
    public ResponseEntity<byte[]> exportStudentAttendancePdf(@PathVariable Long studentId) throws IOException {
        byte[] pdfData = pdfExportService.exportStudentAttendanceReport(studentId);
        String filename = "student_attendance_" + studentId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";
        
        return createPdfResponse(pdfData, filename);
    }

    @GetMapping("/pdf/test-results/{testId}")
    public ResponseEntity<byte[]> exportTestResultsPdf(@PathVariable Long testId) throws IOException {
        byte[] pdfData = pdfExportService.exportTestResultReport(testId);
        String filename = "test_results_" + testId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";
        
        return createPdfResponse(pdfData, filename);
    }

    @GetMapping("/pdf/attendance-certificate/{studentId}")
    public ResponseEntity<byte[]> exportAttendanceCertificate(@PathVariable Long studentId) throws IOException {
        byte[] pdfData = pdfExportService.exportAttendanceCertificate(studentId);
        String filename = "attendance_certificate_" + studentId + "_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";
        
        return createPdfResponse(pdfData, filename);
    }

    private ResponseEntity<byte[]> createExcelResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(data.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}