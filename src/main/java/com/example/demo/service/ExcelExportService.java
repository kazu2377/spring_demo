package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private TestScoreRepository testScoreRepository;
    
    @Autowired
    private SchoolClassRepository schoolClassRepository;
    
    @Autowired
    private TestRepository testRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public byte[] exportStudentAttendanceReport(Long studentId) throws IOException {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // スタイル設定
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            
            // 基本情報シート
            Sheet infoSheet = workbook.createSheet("受講生情報");
            createStudentInfoSheet(infoSheet, student, headerStyle, centerStyle);
            
            // 出席記録シート
            Sheet attendanceSheet = workbook.createSheet("出席記録");
            createAttendanceSheet(attendanceSheet, attendances, headerStyle, dateStyle, centerStyle);
            
            // 統計シート
            Sheet statsSheet = workbook.createSheet("出席統計");
            createAttendanceStatsSheet(statsSheet, student, attendances, headerStyle, centerStyle);
            
            // バイト配列として出力
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportTestResultsReport(Long testId) throws IOException {
        Test test = testRepository.findById(testId).orElseThrow();
        List<TestScore> testScores = testScoreRepository.findByTestIdOrderByStudentStudentNumber(testId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // テスト情報シート
            Sheet testInfoSheet = workbook.createSheet("テスト情報");
            createTestInfoSheet(testInfoSheet, test, headerStyle, centerStyle);
            
            // 成績一覧シート
            Sheet scoresSheet = workbook.createSheet("成績一覧");
            createTestScoresSheet(scoresSheet, testScores, headerStyle, centerStyle, numberStyle);
            
            // 統計分析シート
            Sheet statsSheet = workbook.createSheet("統計分析");
            createTestStatsSheet(statsSheet, test, testScores, headerStyle, centerStyle, numberStyle);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportClassReport(Long classId) throws IOException {
        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow();
        List<Student> students = studentRepository.findBySchoolClassId(classId);
        List<Test> tests = testRepository.findBySchoolClassId(classId);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // クラス情報シート
            Sheet classInfoSheet = workbook.createSheet("クラス情報");
            createClassInfoSheet(classInfoSheet, schoolClass, headerStyle, centerStyle);
            
            // 受講生一覧シート
            Sheet studentsSheet = workbook.createSheet("受講生一覧");
            createClassStudentsSheet(studentsSheet, students, headerStyle, centerStyle, dateStyle);
            
            // 出席統計シート
            Sheet attendanceStatsSheet = workbook.createSheet("出席統計");
            createClassAttendanceStatsSheet(attendanceStatsSheet, students, headerStyle, centerStyle, numberStyle);
            
            // テスト結果シート
            if (!tests.isEmpty()) {
                Sheet testResultsSheet = workbook.createSheet("テスト結果");
                createClassTestResultsSheet(testResultsSheet, students, tests, headerStyle, centerStyle, numberStyle);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createStudentInfoSheet(Sheet sheet, Student student, CellStyle headerStyle, CellStyle centerStyle) {
        // ヘッダー
        Row headerRow = sheet.createRow(0);
        Cell titleCell = headerRow.createCell(0);
        titleCell.setCellValue("受講生情報詳細");
        titleCell.setCellStyle(headerStyle);
        
        // 基本情報
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "学籍番号", student.getStudentNumber());
        createInfoRow(sheet, rowNum++, "氏名", student.getFullName());
        createInfoRow(sheet, rowNum++, "メールアドレス", student.getEmail());
        createInfoRow(sheet, rowNum++, "入学日", student.getEnrollmentDate().format(DATE_FORMATTER));
        createInfoRow(sheet, rowNum++, "クラス", student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : "未割当");
        createInfoRow(sheet, rowNum++, "ステータス", student.isActive() ? "有効" : "無効");
        
        // 列幅調整
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createAttendanceSheet(Sheet sheet, List<Attendance> attendances, 
                                     CellStyle headerStyle, CellStyle dateStyle, CellStyle centerStyle) {
        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"日付", "出席状況", "備考", "記録者", "記録日時"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // データ行
        int rowNum = 1;
        for (Attendance attendance : attendances) {
            Row row = sheet.createRow(rowNum++);
            
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(attendance.getAttendanceDate().format(DATE_FORMATTER));
            dateCell.setCellStyle(dateStyle);
            
            Cell statusCell = row.createCell(1);
            statusCell.setCellValue(getAttendanceStatusText(attendance.getStatus()));
            statusCell.setCellStyle(centerStyle);
            
            row.createCell(2).setCellValue(attendance.getNotes() != null ? attendance.getNotes() : "");
            row.createCell(3).setCellValue(attendance.getRecordedBy() != null ? attendance.getRecordedBy().getUsername() : "");
            
            Cell recordedAtCell = row.createCell(4);
            if (attendance.getRecordedAt() != null) {
                recordedAtCell.setCellValue(attendance.getRecordedAt().format(DATETIME_FORMATTER));
            }
        }
        
        // 列幅調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAttendanceStatsSheet(Sheet sheet, Student student, List<Attendance> attendances,
                                          CellStyle headerStyle, CellStyle centerStyle) {
        // 統計計算
        long totalDays = attendances.size();
        long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        double attendanceRate = totalDays > 0 ? (double)(presentDays + lateDays) / totalDays * 100 : 0;
        
        // ヘッダー
        Row headerRow = sheet.createRow(0);
        Cell titleCell = headerRow.createCell(0);
        titleCell.setCellValue("出席統計");
        titleCell.setCellStyle(headerStyle);
        
        // 統計データ
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "総授業日数", String.valueOf(totalDays));
        createInfoRow(sheet, rowNum++, "出席日数", String.valueOf(presentDays));
        createInfoRow(sheet, rowNum++, "遅刻回数", String.valueOf(lateDays));
        createInfoRow(sheet, rowNum++, "欠席日数", String.valueOf(absentDays));
        createInfoRow(sheet, rowNum++, "出席率", String.format("%.1f%%", attendanceRate));
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTestInfoSheet(Sheet sheet, Test test, CellStyle headerStyle, CellStyle centerStyle) {
        Row headerRow = sheet.createRow(0);
        Cell titleCell = headerRow.createCell(0);
        titleCell.setCellValue("テスト情報");
        titleCell.setCellStyle(headerStyle);
        
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "テスト名", test.getTestName());
        createInfoRow(sheet, rowNum++, "対象クラス", test.getSchoolClass().getClassName());
        createInfoRow(sheet, rowNum++, "実施日", test.getTestDate().format(DATE_FORMATTER));
        createInfoRow(sheet, rowNum++, "制限時間", test.getDuration() != null ? test.getDuration() + "分" : "制限なし");
        createInfoRow(sheet, rowNum++, "満点", test.getMaxScore() + "点");
        createInfoRow(sheet, rowNum++, "合格点", test.getPassingScore() != null ? test.getPassingScore() + "点" : "未設定");
        createInfoRow(sheet, rowNum++, "テスト種別", test.getTestType().getDisplayName());
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTestScoresSheet(Sheet sheet, List<TestScore> testScores,
                                     CellStyle headerStyle, CellStyle centerStyle, CellStyle numberStyle) {
        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学籍番号", "氏名", "得点", "合否", "コメント", "記録日時"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // データ行
        int rowNum = 1;
        for (TestScore testScore : testScores) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(testScore.getStudent().getStudentNumber());
            row.createCell(1).setCellValue(testScore.getStudent().getFullName());
            
            Cell scoreCell = row.createCell(2);
            scoreCell.setCellValue(testScore.getScore());
            scoreCell.setCellStyle(numberStyle);
            
            Cell passFailCell = row.createCell(3);
            passFailCell.setCellValue(getPassFailText(testScore.getScore(), testScore.getTest().getPassingScore()));
            passFailCell.setCellStyle(centerStyle);
            
            row.createCell(4).setCellValue(testScore.getComment() != null ? testScore.getComment() : "");
            
            if (testScore.getRecordedAt() != null) {
                row.createCell(5).setCellValue(testScore.getRecordedAt().format(DATETIME_FORMATTER));
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTestStatsSheet(Sheet sheet, Test test, List<TestScore> testScores,
                                    CellStyle headerStyle, CellStyle centerStyle, CellStyle numberStyle) {
        if (testScores.isEmpty()) return;
        
        // 統計計算
        double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
        int maxScore = testScores.stream().mapToInt(TestScore::getScore).max().orElse(0);
        int minScore = testScores.stream().mapToInt(TestScore::getScore).min().orElse(0);
        long passedCount = testScores.stream().filter(ts -> {
            Integer passingScore = test.getPassingScore();
            return passingScore != null && ts.getScore() >= passingScore;
        }).count();
        double passRate = (double)passedCount / testScores.size() * 100;
        
        Row headerRow = sheet.createRow(0);
        Cell titleCell = headerRow.createCell(0);
        titleCell.setCellValue("統計分析");
        titleCell.setCellStyle(headerStyle);
        
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "受験者数", String.valueOf(testScores.size()));
        createInfoRow(sheet, rowNum++, "平均点", String.format("%.1f点", averageScore));
        createInfoRow(sheet, rowNum++, "最高点", maxScore + "点");
        createInfoRow(sheet, rowNum++, "最低点", minScore + "点");
        createInfoRow(sheet, rowNum++, "合格者数", String.valueOf(passedCount));
        createInfoRow(sheet, rowNum++, "合格率", String.format("%.1f%%", passRate));
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createClassInfoSheet(Sheet sheet, SchoolClass schoolClass, CellStyle headerStyle, CellStyle centerStyle) {
        Row headerRow = sheet.createRow(0);
        Cell titleCell = headerRow.createCell(0);
        titleCell.setCellValue("クラス情報");
        titleCell.setCellStyle(headerStyle);
        
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "クラス名", schoolClass.getClassName());
        createInfoRow(sheet, rowNum++, "科目", schoolClass.getSubject());
        createInfoRow(sheet, rowNum++, "説明", schoolClass.getDescription() != null ? schoolClass.getDescription() : "");
        createInfoRow(sheet, rowNum++, "開始日", schoolClass.getStartDate().format(DATE_FORMATTER));
        createInfoRow(sheet, rowNum++, "終了日", schoolClass.getEndDate().format(DATE_FORMATTER));
        createInfoRow(sheet, rowNum++, "担当講師", schoolClass.getTeacher() != null ? schoolClass.getTeacher().getUsername() : "未割当");
        createInfoRow(sheet, rowNum++, "定員", schoolClass.getMaxStudents() != null ? schoolClass.getMaxStudents() + "名" : "制限なし");
        createInfoRow(sheet, rowNum++, "現在の受講生数", schoolClass.getCurrentStudentCount() + "名");
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createClassStudentsSheet(Sheet sheet, List<Student> students,
                                        CellStyle headerStyle, CellStyle centerStyle, CellStyle dateStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学籍番号", "氏名", "メールアドレス", "入学日", "ステータス"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentNumber());
            row.createCell(1).setCellValue(student.getFullName());
            row.createCell(2).setCellValue(student.getEmail());
            
            Cell dateCell = row.createCell(3);
            dateCell.setCellValue(student.getEnrollmentDate().format(DATE_FORMATTER));
            dateCell.setCellStyle(dateStyle);
            
            Cell statusCell = row.createCell(4);
            statusCell.setCellValue(student.isActive() ? "有効" : "無効");
            statusCell.setCellStyle(centerStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createClassAttendanceStatsSheet(Sheet sheet, List<Student> students,
                                               CellStyle headerStyle, CellStyle centerStyle, CellStyle numberStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学籍番号", "氏名", "総授業日数", "出席日数", "遅刻回数", "欠席日数", "出席率"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (Student student : students) {
            List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
            
            long totalDays = attendances.size();
            long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            long absentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            double attendanceRate = totalDays > 0 ? (double)(presentDays + lateDays) / totalDays * 100 : 0;
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentNumber());
            row.createCell(1).setCellValue(student.getFullName());
            
            Cell totalCell = row.createCell(2);
            totalCell.setCellValue(totalDays);
            totalCell.setCellStyle(numberStyle);
            
            Cell presentCell = row.createCell(3);
            presentCell.setCellValue(presentDays);
            presentCell.setCellStyle(numberStyle);
            
            Cell lateCell = row.createCell(4);
            lateCell.setCellValue(lateDays);
            lateCell.setCellStyle(numberStyle);
            
            Cell absentCell = row.createCell(5);
            absentCell.setCellValue(absentDays);
            absentCell.setCellStyle(numberStyle);
            
            Cell rateCell = row.createCell(6);
            rateCell.setCellValue(String.format("%.1f%%", attendanceRate));
            rateCell.setCellStyle(centerStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createClassTestResultsSheet(Sheet sheet, List<Student> students, List<Test> tests,
                                           CellStyle headerStyle, CellStyle centerStyle, CellStyle numberStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学籍番号", "氏名", "受験テスト数", "平均点", "最高点", "最低点", "合格テスト数", "合格率"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (Student student : students) {
            List<TestScore> testScores = testScoreRepository.findByStudentId(student.getId());
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentNumber());
            row.createCell(1).setCellValue(student.getFullName());
            
            if (testScores.isEmpty()) {
                row.createCell(2).setCellValue(0);
                row.createCell(3).setCellValue("-");
                row.createCell(4).setCellValue("-");
                row.createCell(5).setCellValue("-");
                row.createCell(6).setCellValue(0);
                row.createCell(7).setCellValue("-");
            } else {
                double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
                int maxScore = testScores.stream().mapToInt(TestScore::getScore).max().orElse(0);
                int minScore = testScores.stream().mapToInt(TestScore::getScore).min().orElse(0);
                long passedTests = testScores.stream().filter(ts -> {
                    Integer passingScore = ts.getTest().getPassingScore();
                    return passingScore != null && ts.getScore() >= passingScore;
                }).count();
                double passRate = (double)passedTests / testScores.size() * 100;
                
                Cell testCountCell = row.createCell(2);
                testCountCell.setCellValue(testScores.size());
                testCountCell.setCellStyle(numberStyle);
                
                Cell avgCell = row.createCell(3);
                avgCell.setCellValue(String.format("%.1f", averageScore));
                avgCell.setCellStyle(numberStyle);
                
                Cell maxCell = row.createCell(4);
                maxCell.setCellValue(maxScore);
                maxCell.setCellStyle(numberStyle);
                
                Cell minCell = row.createCell(5);
                minCell.setCellValue(minScore);
                minCell.setCellStyle(numberStyle);
                
                Cell passedCell = row.createCell(6);
                passedCell.setCellValue(passedTests);
                passedCell.setCellStyle(numberStyle);
                
                Cell passRateCell = row.createCell(7);
                passRateCell.setCellValue(String.format("%.1f%%", passRate));
                passRateCell.setCellStyle(centerStyle);
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private String getAttendanceStatusText(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "出席";
            case ABSENT -> "欠席";
            case LATE -> "遅刻";
            case EARLY_LEAVE -> "早退";
            case EXCUSED -> "公欠";
        };
    }

    private String getPassFailText(Integer score, Integer passingScore) {
        if (passingScore == null) {
            return "-";
        }
        return score >= passingScore ? "合格" : "不合格";
    }
}