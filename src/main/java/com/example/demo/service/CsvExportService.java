package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvExportService {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private TestScoreRepository testScoreRepository;
    
    @Autowired
    private SchoolClassRepository schoolClassRepository;

    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_NEWLINE = "\n";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public String exportStudentsCsv() {
        List<Student> students = studentRepository.findAll();
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("学籍番号,姓,名,フルネーム,メールアドレス,入学日,クラス名,ステータス");
        writer.append(CSV_NEWLINE);
        
        // データ
        for (Student student : students) {
            writer.append(escapeSpecialCharacters(student.getStudentNumber()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getLastName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getFirstName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getFullName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getEmail()));
            writer.append(CSV_SEPARATOR);
            writer.append(student.getEnrollmentDate().format(DATE_FORMATTER));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(student.isActive() ? "有効" : "無効");
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    public String exportClassesCsv() {
        List<SchoolClass> classes = schoolClassRepository.findAll();
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("クラス名,科目,説明,開始日,終了日,講師名,最大受講生数,現在の受講生数,ステータス");
        writer.append(CSV_NEWLINE);
        
        // データ
        for (SchoolClass schoolClass : classes) {
            writer.append(escapeSpecialCharacters(schoolClass.getClassName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(schoolClass.getSubject()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(schoolClass.getDescription() != null ? schoolClass.getDescription() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(schoolClass.getStartDate().format(DATE_FORMATTER));
            writer.append(CSV_SEPARATOR);
            writer.append(schoolClass.getEndDate().format(DATE_FORMATTER));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(schoolClass.getTeacher() != null ? schoolClass.getTeacher().getUsername() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(schoolClass.getMaxStudents() != null ? schoolClass.getMaxStudents().toString() : "");
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(schoolClass.getCurrentStudentCount()));
            writer.append(CSV_SEPARATOR);
            writer.append(schoolClass.isActive() ? "有効" : "無効");
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    public String exportAttendanceCsv(Long classId) {
        List<Attendance> attendances;
        if (classId != null) {
            attendances = attendanceRepository.findBySchoolClassIdOrderByAttendanceDateDesc(classId);
        } else {
            attendances = attendanceRepository.findAllByOrderByAttendanceDateDesc();
        }
        
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("日付,学籍番号,受講生名,クラス名,出席状況,備考,記録者,記録日時");
        writer.append(CSV_NEWLINE);
        
        // データ
        for (Attendance attendance : attendances) {
            writer.append(attendance.getAttendanceDate().format(DATE_FORMATTER));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(attendance.getStudent().getStudentNumber()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(attendance.getStudent().getFullName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(attendance.getSchoolClass().getClassName()));
            writer.append(CSV_SEPARATOR);
            writer.append(getAttendanceStatusText(attendance.getStatus()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(attendance.getNotes() != null ? attendance.getNotes() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(attendance.getRecordedBy() != null ? attendance.getRecordedBy().getUsername() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(attendance.getRecordedAt() != null ? attendance.getRecordedAt().format(DATETIME_FORMATTER) : "");
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    public String exportTestScoresCsv(Long testId) {
        List<TestScore> testScores;
        if (testId != null) {
            testScores = testScoreRepository.findByTestIdOrderByStudentStudentNumber(testId);
        } else {
            testScores = testScoreRepository.findAllOrderByTestDateAndStudentNumber();
        }
        
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("テスト名,実施日,学籍番号,受講生名,クラス名,得点,満点,合格点,合否,コメント,記録者,記録日時");
        writer.append(CSV_NEWLINE);
        
        // データ
        for (TestScore testScore : testScores) {
            Test test = testScore.getTest();
            Student student = testScore.getStudent();
            
            writer.append(escapeSpecialCharacters(test.getTestName()));
            writer.append(CSV_SEPARATOR);
            writer.append(test.getTestDate().format(DATE_FORMATTER));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getStudentNumber()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getFullName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(test.getSchoolClass().getClassName()));
            writer.append(CSV_SEPARATOR);
            writer.append(testScore.getScore().toString());
            writer.append(CSV_SEPARATOR);
            writer.append(test.getMaxScore().toString());
            writer.append(CSV_SEPARATOR);
            writer.append(test.getPassingScore() != null ? test.getPassingScore().toString() : "");
            writer.append(CSV_SEPARATOR);
            writer.append(getPassFailText(testScore.getScore(), test.getPassingScore()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(testScore.getComment() != null ? testScore.getComment() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(testScore.getRecordedBy() != null ? testScore.getRecordedBy().getUsername() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(testScore.getRecordedAt() != null ? testScore.getRecordedAt().format(DATETIME_FORMATTER) : "");
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    public String exportStudentAttendanceSummary() {
        List<Student> students = studentRepository.findAll();
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("学籍番号,受講生名,クラス名,総授業日数,出席日数,遅刻回数,欠席日数,出席率");
        writer.append(CSV_NEWLINE);
        
        // 各受講生の出席統計を計算
        for (Student student : students) {
            List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
            
            long totalDays = attendances.size();
            long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            long absentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            
            double attendanceRate = totalDays > 0 ? (double)(presentDays + lateDays) / totalDays * 100 : 0;
            
            writer.append(escapeSpecialCharacters(student.getStudentNumber()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getFullName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(totalDays));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(presentDays));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(lateDays));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(absentDays));
            writer.append(CSV_SEPARATOR);
            writer.append(String.format("%.1f%%", attendanceRate));
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    public String exportTestResultsSummary() {
        List<Student> students = studentRepository.findAll();
        StringWriter writer = new StringWriter();
        
        // ヘッダー
        writer.append("学籍番号,受講生名,クラス名,受験テスト数,平均点,最高点,最低点,合格テスト数,合格率");
        writer.append(CSV_NEWLINE);
        
        // 各受講生のテスト結果統計を計算
        for (Student student : students) {
            List<TestScore> testScores = testScoreRepository.findByStudentId(student.getId());
            
            if (testScores.isEmpty()) {
                writer.append(escapeSpecialCharacters(student.getStudentNumber()));
                writer.append(CSV_SEPARATOR);
                writer.append(escapeSpecialCharacters(student.getFullName()));
                writer.append(CSV_SEPARATOR);
                writer.append(escapeSpecialCharacters(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : ""));
                writer.append(CSV_SEPARATOR);
                writer.append("0,0,0,0,0,0%");
                writer.append(CSV_NEWLINE);
                continue;
            }
            
            double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
            int maxScore = testScores.stream().mapToInt(TestScore::getScore).max().orElse(0);
            int minScore = testScores.stream().mapToInt(TestScore::getScore).min().orElse(0);
            
            long passedTests = testScores.stream().filter(ts -> {
                Integer passingScore = ts.getTest().getPassingScore();
                return passingScore != null && ts.getScore() >= passingScore;
            }).count();
            
            double passRate = testScores.size() > 0 ? (double)passedTests / testScores.size() * 100 : 0;
            
            writer.append(escapeSpecialCharacters(student.getStudentNumber()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getFullName()));
            writer.append(CSV_SEPARATOR);
            writer.append(escapeSpecialCharacters(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : ""));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(testScores.size()));
            writer.append(CSV_SEPARATOR);
            writer.append(String.format("%.1f", averageScore));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(maxScore));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(minScore));
            writer.append(CSV_SEPARATOR);
            writer.append(String.valueOf(passedTests));
            writer.append(CSV_SEPARATOR);
            writer.append(String.format("%.1f%%", passRate));
            writer.append(CSV_NEWLINE);
        }
        
        return writer.toString();
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
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
            return "";
        }
        return score >= passingScore ? "合格" : "不合格";
    }
}