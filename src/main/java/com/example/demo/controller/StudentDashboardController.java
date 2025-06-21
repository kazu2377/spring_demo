package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.entity.TestScore;
import com.example.demo.entity.User;
import com.example.demo.service.StudentService;
import com.example.demo.service.TestScoreService;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TestScoreService testScoreService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        // 受講生情報を取得（UserエンティティからStudentを逆引き）
        Student student = findStudentByUser(currentUser);
        
        if (student == null) {
            model.addAttribute("error", "受講生情報が見つかりません。管理者にお問い合わせください。");
            return "student/dashboard";
        }
        
        // 成績情報
        List<TestScore> recentScores = testScoreService.getTestScoresByStudent(student);
        TestScoreService.StudentGradeReport gradeReport = testScoreService.getStudentGradeReport(student);
        
        // 出席情報
        AttendanceService.AttendanceSummary attendanceSummary = attendanceService.getAttendanceSummary(student);
        
        model.addAttribute("student", student);
        model.addAttribute("recentScores", recentScores);
        model.addAttribute("gradeReport", gradeReport);
        model.addAttribute("attendanceSummary", attendanceSummary);
        
        return "student/dashboard";
    }
    
    @GetMapping("/scores")
    public String viewScores(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        Student student = findStudentByUser(currentUser);
        if (student == null) {
            model.addAttribute("error", "受講生情報が見つかりません。");
            return "student/scores";
        }
        
        List<TestScore> allScores = testScoreService.getTestScoresByStudent(student);
        TestScoreService.StudentGradeReport gradeReport = testScoreService.getStudentGradeReport(student);
        
        model.addAttribute("student", student);
        model.addAttribute("scores", allScores);
        model.addAttribute("gradeReport", gradeReport);
        
        return "student/scores";
    }
    
    @GetMapping("/attendance")
    public String viewAttendance(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        Student student = findStudentByUser(currentUser);
        if (student == null) {
            model.addAttribute("error", "受講生情報が見つかりません。");
            return "student/attendance";
        }
        
        AttendanceService.AttendanceSummary attendanceSummary = attendanceService.getAttendanceSummary(student);
        
        model.addAttribute("student", student);
        model.addAttribute("attendanceSummary", attendanceSummary);
        
        return "student/attendance";
    }
    
    private Student findStudentByUser(User user) {
        // UserエンティティからStudentを逆引きする簡易的な実装
        // 実際の実装では、StudentエンティティのUserとの関連を利用するか、
        // 専用のクエリメソッドを作成することが推奨されます
        List<Student> allStudents = studentService.getAllStudents();
        return allStudents.stream()
            .filter(student -> student.getUser() != null && student.getUser().getId().equals(user.getId()))
            .findFirst()
            .orElse(null);
    }
}