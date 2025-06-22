package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private TestScoreRepository testScoreRepository;
    
    @Autowired
    private TestRepository testRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分");

    public byte[] exportStudentAttendanceReport(Long studentId) throws IOException {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // 日本語フォント設定
            PdfFont font = PdfFontFactory.createFont("HeiseiMin-W3", "UniJIS-UCS2-H");
            document.setFont(font);
            
            // タイトル
            Paragraph title = new Paragraph("出席状況報告書")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // 受講生情報
            addStudentInfo(document, student);
            
            // 出席統計
            addAttendanceStats(document, attendances);
            
            // 出席記録
            addAttendanceRecords(document, attendances);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }

    public byte[] exportTestResultReport(Long testId) throws IOException {
        Test test = testRepository.findById(testId).orElseThrow();
        List<TestScore> testScores = testScoreRepository.findByTestIdOrderByStudentStudentNumber(testId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            PdfFont font = PdfFontFactory.createFont("HeiseiMin-W3", "UniJIS-UCS2-H");
            document.setFont(font);
            
            // タイトル
            Paragraph title = new Paragraph("テスト結果報告書")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            
            // テスト情報
            addTestInfo(document, test);
            
            // 統計情報
            addTestStats(document, test, testScores);
            
            // 成績一覧
            addTestScoresList(document, testScores);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }

    public byte[] exportAttendanceCertificate(Long studentId) throws IOException {
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            PdfFont font = PdfFontFactory.createFont("HeiseiMin-W3", "UniJIS-UCS2-H");
            document.setFont(font);
            
            // タイトル
            Paragraph title = new Paragraph("出席証明書")
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(40);
            document.add(title);
            
            // 証明書本文
            addCertificateContent(document, student, attendances);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }

    private void addStudentInfo(Document document, Student student) {
        Paragraph header = new Paragraph("受講生情報")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addCell(createCell("学籍番号", true));
        table.addCell(createCell(student.getStudentNumber(), false));
        
        table.addCell(createCell("氏名", true));
        table.addCell(createCell(student.getFullName(), false));
        
        table.addCell(createCell("メールアドレス", true));
        table.addCell(createCell(student.getEmail(), false));
        
        table.addCell(createCell("入学日", true));
        table.addCell(createCell(student.getEnrollmentDate().format(DATE_FORMATTER), false));
        
        table.addCell(createCell("クラス", true));
        table.addCell(createCell(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : "未割当", false));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addAttendanceStats(Document document, List<Attendance> attendances) {
        Paragraph header = new Paragraph("出席統計")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        long totalDays = attendances.size();
        long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        double attendanceRate = totalDays > 0 ? (double)(presentDays + lateDays) / totalDays * 100 : 0;
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addCell(createCell("総授業日数", true));
        table.addCell(createCell(totalDays + "日", false));
        
        table.addCell(createCell("出席日数", true));
        table.addCell(createCell(presentDays + "日", false));
        
        table.addCell(createCell("遅刻回数", true));
        table.addCell(createCell(lateDays + "回", false));
        
        table.addCell(createCell("欠席日数", true));
        table.addCell(createCell(absentDays + "日", false));
        
        table.addCell(createCell("出席率", true));
        table.addCell(createCell(String.format("%.1f%%", attendanceRate), false));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addAttendanceRecords(Document document, List<Attendance> attendances) {
        Paragraph header = new Paragraph("出席記録詳細")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{20, 15, 35, 15, 15}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // ヘッダー
        table.addHeaderCell(createCell("日付", true));
        table.addHeaderCell(createCell("出席状況", true));
        table.addHeaderCell(createCell("備考", true));
        table.addHeaderCell(createCell("記録者", true));
        table.addHeaderCell(createCell("記録日時", true));
        
        // データ行（最新20件のみ表示）
        List<Attendance> recentAttendances = attendances.stream().limit(20).toList();
        for (Attendance attendance : recentAttendances) {
            table.addCell(createCell(attendance.getAttendanceDate().format(DATE_FORMATTER), false));
            table.addCell(createCell(getAttendanceStatusText(attendance.getStatus()), false));
            table.addCell(createCell(attendance.getNotes() != null ? attendance.getNotes() : "", false));
            table.addCell(createCell(attendance.getRecordedBy() != null ? attendance.getRecordedBy().getUsername() : "", false));
            table.addCell(createCell(attendance.getRecordedAt() != null ? attendance.getRecordedAt().format(DATETIME_FORMATTER) : "", false));
        }
        
        document.add(table);
        
        if (attendances.size() > 20) {
            document.add(new Paragraph("※ 最新20件のみ表示しています。")
                    .setFontSize(10)
                    .setMarginTop(5));
        }
    }

    private void addTestInfo(Document document, Test test) {
        Paragraph header = new Paragraph("テスト情報")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addCell(createCell("テスト名", true));
        table.addCell(createCell(test.getTestName(), false));
        
        table.addCell(createCell("対象クラス", true));
        table.addCell(createCell(test.getSchoolClass().getClassName(), false));
        
        table.addCell(createCell("実施日", true));
        table.addCell(createCell(test.getTestDate().format(DATE_FORMATTER), false));
        
        table.addCell(createCell("制限時間", true));
        table.addCell(createCell(test.getDuration() != null ? test.getDuration() + "分" : "制限なし", false));
        
        table.addCell(createCell("満点", true));
        table.addCell(createCell(test.getMaxScore() + "点", false));
        
        table.addCell(createCell("合格点", true));
        table.addCell(createCell(test.getPassingScore() != null ? test.getPassingScore() + "点" : "未設定", false));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTestStats(Document document, Test test, List<TestScore> testScores) {
        if (testScores.isEmpty()) return;
        
        Paragraph header = new Paragraph("統計情報")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
        int maxScore = testScores.stream().mapToInt(TestScore::getScore).max().orElse(0);
        int minScore = testScores.stream().mapToInt(TestScore::getScore).min().orElse(0);
        long passedCount = testScores.stream().filter(ts -> {
            Integer passingScore = test.getPassingScore();
            return passingScore != null && ts.getScore() >= passingScore;
        }).count();
        double passRate = (double)passedCount / testScores.size() * 100;
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        
        table.addCell(createCell("受験者数", true));
        table.addCell(createCell(testScores.size() + "名", false));
        
        table.addCell(createCell("平均点", true));
        table.addCell(createCell(String.format("%.1f点", averageScore), false));
        
        table.addCell(createCell("最高点", true));
        table.addCell(createCell(maxScore + "点", false));
        
        table.addCell(createCell("最低点", true));
        table.addCell(createCell(minScore + "点", false));
        
        table.addCell(createCell("合格者数", true));
        table.addCell(createCell(passedCount + "名", false));
        
        table.addCell(createCell("合格率", true));
        table.addCell(createCell(String.format("%.1f%%", passRate), false));
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTestScoresList(Document document, List<TestScore> testScores) {
        Paragraph header = new Paragraph("成績一覧")
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(header);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 25, 15, 15, 30}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // ヘッダー
        table.addHeaderCell(createCell("学籍番号", true));
        table.addHeaderCell(createCell("氏名", true));
        table.addHeaderCell(createCell("得点", true));
        table.addHeaderCell(createCell("合否", true));
        table.addHeaderCell(createCell("コメント", true));
        
        // データ行
        for (TestScore testScore : testScores) {
            table.addCell(createCell(testScore.getStudent().getStudentNumber(), false));
            table.addCell(createCell(testScore.getStudent().getFullName(), false));
            table.addCell(createCell(testScore.getScore() + "点", false));
            table.addCell(createCell(getPassFailText(testScore.getScore(), testScore.getTest().getPassingScore()), false));
            table.addCell(createCell(testScore.getComment() != null ? testScore.getComment() : "", false));
        }
        
        document.add(table);
    }

    private void addCertificateContent(Document document, Student student, List<Attendance> attendances) {
        // 受講生情報
        Paragraph studentInfo = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        studentInfo.add("学籍番号：" + student.getStudentNumber() + "\n");
        studentInfo.add("氏　　名：" + student.getFullName() + "\n");
        if (student.getSchoolClass() != null) {
            studentInfo.add("クラス名：" + student.getSchoolClass().getClassName());
        }
        document.add(studentInfo);
        
        // 出席統計
        long totalDays = attendances.size();
        long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        double attendanceRate = totalDays > 0 ? (double)(presentDays + lateDays) / totalDays * 100 : 0;
        
        Paragraph content = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(40);
        content.add("上記の者は、当校において下記の通り出席したことを証明します。\n\n");
        content.add("総授業日数：" + totalDays + "日\n");
        content.add("出席日数：" + presentDays + "日\n");
        content.add("遅刻回数：" + lateDays + "回\n");
        content.add("出席率：" + String.format("%.1f%%", attendanceRate) + "\n\n");
        document.add(content);
        
        // 発行日
        Paragraph issueDate = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(40);
        issueDate.add("発行日：" + java.time.LocalDate.now().format(DATE_FORMATTER));
        document.add(issueDate);
        
        // 発行者
        Paragraph issuer = new Paragraph()
                .setTextAlignment(TextAlignment.CENTER);
        issuer.add("職業訓練校管理システム\n");
        issuer.add("校長　　　　　　　　　印");
        document.add(issuer);
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content));
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
        }
        return cell;
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