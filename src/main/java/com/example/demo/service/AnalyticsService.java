package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

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

    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本統計
        stats.put("totalStudents", studentRepository.countByActiveTrue());
        stats.put("totalClasses", schoolClassRepository.countByActiveTrue());
        stats.put("totalTests", testRepository.countByActiveTrue());
        stats.put("totalAttendanceRecords", attendanceRepository.count());
        
        // 出席統計
        List<Attendance> allAttendances = attendanceRepository.findAll();
        stats.put("overallAttendanceRate", calculateOverallAttendanceRate(allAttendances));
        
        // 成績統計
        List<TestScore> allTestScores = testScoreRepository.findAll();
        stats.put("overallAverageScore", calculateOverallAverageScore(allTestScores));
        
        // 月別データ
        stats.put("monthlyAttendanceData", getMonthlyAttendanceData());
        stats.put("monthlyScoreData", getMonthlyScoreData());
        
        return stats;
    }

    public Map<String, Object> getClassAnalytics(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow();
        List<Student> students = studentRepository.findBySchoolClassId(classId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("classInfo", getClassBasicInfo(schoolClass));
        analytics.put("attendanceAnalytics", getClassAttendanceAnalytics(students));
        analytics.put("testAnalytics", getClassTestAnalytics(classId));
        analytics.put("studentPerformance", getStudentPerformanceAnalytics(students));
        analytics.put("trends", getClassTrends(classId));
        
        return analytics;
    }

    public Map<String, Object> getStudentAnalytics(Long studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("studentInfo", getStudentBasicInfo(student));
        analytics.put("attendanceAnalytics", getStudentAttendanceAnalytics(studentId));
        analytics.put("testAnalytics", getStudentTestAnalytics(studentId));
        analytics.put("progressAnalytics", getStudentProgressAnalytics(studentId));
        analytics.put("recommendations", getStudentRecommendations(studentId));
        
        return analytics;
    }

    public List<Map<String, Object>> getAttendanceRiskStudents() {
        List<Student> students = studentRepository.findByActiveTrue();
        List<Map<String, Object>> riskStudents = new ArrayList<>();
        
        for (Student student : students) {
            List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
            if (attendances.isEmpty()) continue;
            
            long totalDays = attendances.size();
            long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            double attendanceRate = (double)(presentDays + lateDays) / totalDays * 100;
            
            // 出席率が80%未満の学生をリスクとして識別
            if (attendanceRate < 80) {
                Map<String, Object> riskStudent = new HashMap<>();
                riskStudent.put("student", student);
                riskStudent.put("attendanceRate", attendanceRate);
                riskStudent.put("totalDays", totalDays);
                riskStudent.put("absentDays", totalDays - presentDays - lateDays);
                riskStudent.put("riskLevel", getRiskLevel(attendanceRate));
                riskStudents.add(riskStudent);
            }
        }
        
        // リスクレベル順にソート
        riskStudents.sort((a, b) -> Double.compare((Double)a.get("attendanceRate"), (Double)b.get("attendanceRate")));
        
        return riskStudents;
    }

    public List<Map<String, Object>> getAcademicRiskStudents() {
        List<Student> students = studentRepository.findByActiveTrue();
        List<Map<String, Object>> riskStudents = new ArrayList<>();
        
        for (Student student : students) {
            List<TestScore> testScores = testScoreRepository.findByStudentId(student.getId());
            if (testScores.isEmpty()) continue;
            
            double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
            long failedTests = testScores.stream().filter(ts -> {
                Integer passingScore = ts.getTest().getPassingScore();
                return passingScore != null && ts.getScore() < passingScore;
            }).count();
            double failureRate = (double)failedTests / testScores.size() * 100;
            
            // 平均点が60点未満または不合格率が50%以上の学生をリスクとして識別
            if (averageScore < 60 || failureRate >= 50) {
                Map<String, Object> riskStudent = new HashMap<>();
                riskStudent.put("student", student);
                riskStudent.put("averageScore", averageScore);
                riskStudent.put("totalTests", testScores.size());
                riskStudent.put("failedTests", failedTests);
                riskStudent.put("failureRate", failureRate);
                riskStudent.put("riskLevel", getAcademicRiskLevel(averageScore, failureRate));
                riskStudents.add(riskStudent);
            }
        }
        
        // リスクレベル順にソート
        riskStudents.sort((a, b) -> Double.compare((Double)a.get("averageScore"), (Double)b.get("averageScore")));
        
        return riskStudents;
    }

    public Map<String, Object> getPerformanceTrends(int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("attendanceTrend", getAttendanceTrend(startDate, endDate));
        trends.put("scoreTrend", getScoreTrend(startDate, endDate));
        trends.put("enrollmentTrend", getEnrollmentTrend(startDate, endDate));
        
        return trends;
    }

    private double calculateOverallAttendanceRate(List<Attendance> attendances) {
        if (attendances.isEmpty()) return 0;
        
        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();
        
        return (double)presentCount / attendances.size() * 100;
    }

    private double calculateOverallAverageScore(List<TestScore> testScores) {
        if (testScores.isEmpty()) return 0;
        
        return testScores.stream()
                .mapToInt(TestScore::getScore)
                .average()
                .orElse(0);
    }

    private List<Map<String, Object>> getMonthlyAttendanceData() {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate startDate = month.withDayOfMonth(1);
            LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
            
            List<Attendance> monthAttendances = attendanceRepository
                    .findByAttendanceDateBetween(startDate, endDate);
            
            double attendanceRate = calculateOverallAttendanceRate(monthAttendances);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy/MM")));
            monthData.put("attendanceRate", Math.round(attendanceRate * 10) / 10.0);
            monthData.put("totalRecords", monthAttendances.size());
            
            monthlyData.add(monthData);
        }
        
        return monthlyData;
    }

    private List<Map<String, Object>> getMonthlyScoreData() {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate startDate = month.withDayOfMonth(1);
            LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
            
            List<TestScore> monthScores = testScoreRepository
                    .findByRecordedAtBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            
            double averageScore = monthScores.stream()
                    .mapToInt(TestScore::getScore)
                    .average()
                    .orElse(0);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy/MM")));
            monthData.put("averageScore", Math.round(averageScore * 10) / 10.0);
            monthData.put("totalTests", monthScores.size());
            
            monthlyData.add(monthData);
        }
        
        return monthlyData;
    }

    private Map<String, Object> getClassBasicInfo(SchoolClass schoolClass) {
        Map<String, Object> info = new HashMap<>();
        info.put("className", schoolClass.getClassName());
        info.put("subject", schoolClass.getSubject());
        info.put("currentStudentCount", schoolClass.getCurrentStudentCount());
        info.put("maxStudents", schoolClass.getMaxStudents());
        info.put("startDate", schoolClass.getStartDate());
        info.put("endDate", schoolClass.getEndDate());
        info.put("teacher", schoolClass.getTeacher() != null ? schoolClass.getTeacher().getUsername() : null);
        
        return info;
    }

    private Map<String, Object> getClassAttendanceAnalytics(List<Student> students) {
        Map<String, Object> analytics = new HashMap<>();
        List<Double> attendanceRates = new ArrayList<>();
        
        for (Student student : students) {
            List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
            if (!attendances.isEmpty()) {
                long totalDays = attendances.size();
                long presentDays = attendances.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                        .count();
                double rate = (double)presentDays / totalDays * 100;
                attendanceRates.add(rate);
            }
        }
        
        if (!attendanceRates.isEmpty()) {
            analytics.put("averageAttendanceRate", attendanceRates.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            analytics.put("minAttendanceRate", Collections.min(attendanceRates));
            analytics.put("maxAttendanceRate", Collections.max(attendanceRates));
            analytics.put("studentsWithGoodAttendance", attendanceRates.stream().filter(rate -> rate >= 90).count());
            analytics.put("studentsAtRisk", attendanceRates.stream().filter(rate -> rate < 80).count());
        } else {
            analytics.put("averageAttendanceRate", 0);
            analytics.put("minAttendanceRate", 0);
            analytics.put("maxAttendanceRate", 0);
            analytics.put("studentsWithGoodAttendance", 0);
            analytics.put("studentsAtRisk", 0);
        }
        
        return analytics;
    }

    private Map<String, Object> getClassTestAnalytics(Long classId) {
        List<Test> tests = testRepository.findBySchoolClassId(classId);
        Map<String, Object> analytics = new HashMap<>();
        
        if (tests.isEmpty()) {
            analytics.put("totalTests", 0);
            analytics.put("averageScore", 0);
            return analytics;
        }
        
        List<TestScore> allClassScores = new ArrayList<>();
        for (Test test : tests) {
            allClassScores.addAll(testScoreRepository.findByTestId(test.getId()));
        }
        
        analytics.put("totalTests", tests.size());
        analytics.put("totalScores", allClassScores.size());
        
        if (!allClassScores.isEmpty()) {
            double averageScore = allClassScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
            analytics.put("averageScore", averageScore);
            analytics.put("minScore", allClassScores.stream().mapToInt(TestScore::getScore).min().orElse(0));
            analytics.put("maxScore", allClassScores.stream().mapToInt(TestScore::getScore).max().orElse(0));
        } else {
            analytics.put("averageScore", 0);
            analytics.put("minScore", 0);
            analytics.put("maxScore", 0);
        }
        
        return analytics;
    }

    private List<Map<String, Object>> getStudentPerformanceAnalytics(List<Student> students) {
        return students.stream().map(student -> {
            Map<String, Object> performance = new HashMap<>();
            performance.put("student", student);
            
            // 出席分析
            List<Attendance> attendances = attendanceRepository.findByStudentId(student.getId());
            if (!attendances.isEmpty()) {
                long totalDays = attendances.size();
                long presentDays = attendances.stream()
                        .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                        .count();
                performance.put("attendanceRate", (double)presentDays / totalDays * 100);
            } else {
                performance.put("attendanceRate", 0);
            }
            
            // 成績分析
            List<TestScore> testScores = testScoreRepository.findByStudentId(student.getId());
            if (!testScores.isEmpty()) {
                double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
                performance.put("averageScore", averageScore);
                performance.put("testCount", testScores.size());
            } else {
                performance.put("averageScore", 0);
                performance.put("testCount", 0);
            }
            
            return performance;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> getClassTrends(Long classId) {
        Map<String, Object> trends = new HashMap<>();
        
        // 過去6ヶ月の傾向を分析
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> monthlyTrends = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate startDate = month.withDayOfMonth(1);
            LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
            
            // その月の出席データ
            List<Attendance> monthAttendances = attendanceRepository
                    .findBySchoolClassIdAndAttendanceDateBetween(classId, startDate, endDate);
            
            double attendanceRate = calculateOverallAttendanceRate(monthAttendances);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy/MM")));
            monthData.put("attendanceRate", Math.round(attendanceRate * 10) / 10.0);
            
            monthlyTrends.add(monthData);
        }
        
        trends.put("monthlyAttendance", monthlyTrends);
        
        return trends;
    }

    private Map<String, Object> getStudentBasicInfo(Student student) {
        Map<String, Object> info = new HashMap<>();
        info.put("studentNumber", student.getStudentNumber());
        info.put("fullName", student.getFullName());
        info.put("email", student.getEmail());
        info.put("enrollmentDate", student.getEnrollmentDate());
        info.put("className", student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : null);
        
        return info;
    }

    private Map<String, Object> getStudentAttendanceAnalytics(Long studentId) {
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        Map<String, Object> analytics = new HashMap<>();
        
        if (attendances.isEmpty()) {
            analytics.put("totalDays", 0);
            analytics.put("attendanceRate", 0);
            return analytics;
        }
        
        long totalDays = attendances.size();
        long presentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absentDays = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        
        analytics.put("totalDays", totalDays);
        analytics.put("presentDays", presentDays);
        analytics.put("lateDays", lateDays);
        analytics.put("absentDays", absentDays);
        analytics.put("attendanceRate", (double)(presentDays + lateDays) / totalDays * 100);
        
        return analytics;
    }

    private Map<String, Object> getStudentTestAnalytics(Long studentId) {
        List<TestScore> testScores = testScoreRepository.findByStudentId(studentId);
        Map<String, Object> analytics = new HashMap<>();
        
        if (testScores.isEmpty()) {
            analytics.put("totalTests", 0);
            analytics.put("averageScore", 0);
            return analytics;
        }
        
        double averageScore = testScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
        int maxScore = testScores.stream().mapToInt(TestScore::getScore).max().orElse(0);
        int minScore = testScores.stream().mapToInt(TestScore::getScore).min().orElse(0);
        
        long passedTests = testScores.stream().filter(ts -> {
            Integer passingScore = ts.getTest().getPassingScore();
            return passingScore != null && ts.getScore() >= passingScore;
        }).count();
        
        analytics.put("totalTests", testScores.size());
        analytics.put("averageScore", averageScore);
        analytics.put("maxScore", maxScore);
        analytics.put("minScore", minScore);
        analytics.put("passedTests", passedTests);
        analytics.put("passRate", (double)passedTests / testScores.size() * 100);
        
        return analytics;
    }

    private Map<String, Object> getStudentProgressAnalytics(Long studentId) {
        Map<String, Object> progress = new HashMap<>();
        
        // 最近6ヶ月の成績推移
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> monthlyProgress = new ArrayList<>();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate startDate = month.withDayOfMonth(1);
            LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
            
            List<TestScore> monthScores = testScoreRepository
                    .findByStudentIdAndRecordedAtBetween(studentId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            
            double averageScore = monthScores.stream().mapToInt(TestScore::getScore).average().orElse(0);
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.format(DateTimeFormatter.ofPattern("yyyy/MM")));
            monthData.put("averageScore", Math.round(averageScore * 10) / 10.0);
            monthData.put("testCount", monthScores.size());
            
            monthlyProgress.add(monthData);
        }
        
        progress.put("monthlyScores", monthlyProgress);
        
        return progress;
    }

    private List<String> getStudentRecommendations(Long studentId) {
        List<String> recommendations = new ArrayList<>();
        
        // 出席状況に基づく推奨
        Map<String, Object> attendanceAnalytics = getStudentAttendanceAnalytics(studentId);
        double attendanceRate = (Double) attendanceAnalytics.get("attendanceRate");
        
        if (attendanceRate < 70) {
            recommendations.add("出席率が低下しています。学習相談やサポートが必要です。");
        } else if (attendanceRate < 85) {
            recommendations.add("出席率の向上に注意が必要です。");
        } else if (attendanceRate >= 95) {
            recommendations.add("優秀な出席率です。継続してください。");
        }
        
        // 成績に基づく推奨
        Map<String, Object> testAnalytics = getStudentTestAnalytics(studentId);
        if (testAnalytics.get("totalTests").equals(0)) {
            recommendations.add("テスト受験がありません。");
        } else {
            double averageScore = (Double) testAnalytics.get("averageScore");
            double passRate = (Double) testAnalytics.get("passRate");
            
            if (averageScore < 50) {
                recommendations.add("成績が低迷しています。基礎から復習することをお勧めします。");
            } else if (averageScore < 70) {
                recommendations.add("成績向上のため追加学習をお勧めします。");
            } else if (averageScore >= 85) {
                recommendations.add("優秀な成績です。さらなる挑戦をしてみてください。");
            }
            
            if (passRate < 50) {
                recommendations.add("不合格率が高いです。学習方法の見直しが必要です。");
            }
        }
        
        return recommendations;
    }

    private String getRiskLevel(double attendanceRate) {
        if (attendanceRate < 60) return "高";
        if (attendanceRate < 75) return "中";
        return "低";
    }

    private String getAcademicRiskLevel(double averageScore, double failureRate) {
        if (averageScore < 50 || failureRate >= 70) return "高";
        if (averageScore < 65 || failureRate >= 50) return "中";
        return "低";
    }

    private List<Map<String, Object>> getAttendanceTrend(LocalDate startDate, LocalDate endDate) {
        // 実装省略（月別出席率推移）
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getScoreTrend(LocalDate startDate, LocalDate endDate) {
        // 実装省略（月別平均点推移）
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getEnrollmentTrend(LocalDate startDate, LocalDate endDate) {
        // 実装省略（月別入学者数推移）
        return new ArrayList<>();
    }
}