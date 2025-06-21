package com.example.demo.controller;

import com.example.demo.entity.Test;
import com.example.demo.entity.TestScore;
import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.service.TestService;
import com.example.demo.service.TestScoreService;
import com.example.demo.service.StudentService;
import com.example.demo.service.SchoolClassService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tests")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class TestController {
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private TestScoreService testScoreService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SchoolClassService schoolClassService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listTests(@RequestParam(value = "classId", required = false) Long classId,
                           @RequestParam(value = "type", required = false) Test.TestType type,
                           Model model) {
        
        List<Test> tests;
        
        if (classId != null && type != null) {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            tests = testService.getTestsByClassAndType(schoolClass, type);
        } else if (classId != null) {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            tests = testService.getTestsByClass(schoolClass);
        } else if (type != null) {
            tests = testService.getTestsByType(type);
        } else {
            tests = testService.getActiveTests();
        }
        
        model.addAttribute("tests", tests);
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        model.addAttribute("testTypes", Test.TestType.values());
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("selectedType", type);
        
        return "tests/list";
    }
    
    @GetMapping("/{id}")
    public String viewTest(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id)
            .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
        
        List<TestScore> scores = testScoreService.getTestScoresByTest(test);
        TestScoreService.TestStatistics statistics = testScoreService.getTestStatistics(test);
        
        model.addAttribute("test", test);
        model.addAttribute("scores", scores);
        model.addAttribute("statistics", statistics);
        
        return "tests/view";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("test", new Test());
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        model.addAttribute("testTypes", Test.TestType.values());
        return "tests/create";
    }
    
    @PostMapping
    public String createTest(@ModelAttribute Test test,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            Test savedTest = testService.createTest(test, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", 
                "テスト「" + savedTest.getTestName() + "」を作成しました");
            return "redirect:/tests/" + savedTest.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/tests/new";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id)
            .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
        
        model.addAttribute("test", test);
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        model.addAttribute("testTypes", Test.TestType.values());
        return "tests/edit";
    }
    
    @PostMapping("/{id}")
    public String updateTest(@PathVariable Long id,
                            @ModelAttribute Test test,
                            RedirectAttributes redirectAttributes) {
        try {
            Test updatedTest = testService.updateTest(id, test);
            redirectAttributes.addFlashAttribute("successMessage", 
                "テスト「" + updatedTest.getTestName() + "」を更新しました");
            return "redirect:/tests/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/tests/" + id + "/edit";
        }
    }
    
    @GetMapping("/{id}/scores")
    public String manageScores(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id)
            .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
        
        List<Student> students = studentService.getStudentsByClass(test.getSchoolClass());
        List<TestScore> existingScores = testScoreService.getTestScoresByTest(test);
        
        Map<Long, TestScore> scoreMap = existingScores.stream()
            .collect(Collectors.toMap(
                score -> score.getStudent().getId(),
                score -> score
            ));
        
        model.addAttribute("test", test);
        model.addAttribute("students", students);
        model.addAttribute("scoreMap", scoreMap);
        
        return "tests/scores";
    }
    
    @PostMapping("/{id}/scores")
    public String recordScores(@PathVariable Long id,
                              @RequestParam Map<String, String> formParams,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            Test test = testService.getTestById(id)
                .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
            
            User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            int recordedCount = 0;
            
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key.startsWith("score_") && !value.isEmpty()) {
                    Long studentId = Long.valueOf(key.substring("score_".length()));
                    Student student = studentService.getStudentById(studentId)
                        .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
                    
                    Integer score = Integer.valueOf(value);
                    String feedback = formParams.getOrDefault("feedback_" + studentId, "");
                    
                    testScoreService.recordScore(student, test, score, feedback, currentUser);
                    recordedCount++;
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                recordedCount + "件の成績を保存しました");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        
        return "redirect:/tests/" + id + "/scores";
    }
    
    @GetMapping("/upcoming")
    public String upcomingTests(Model model) {
        List<Test> upcomingTests = testService.getUpcomingTests();
        List<Test> todaysTests = testService.getTodaysTests();
        List<Test> thisWeeksTests = testService.getThisWeeksTests();
        
        model.addAttribute("upcomingTests", upcomingTests);
        model.addAttribute("todaysTests", todaysTests);
        model.addAttribute("thisWeeksTests", thisWeeksTests);
        
        return "tests/upcoming";
    }
    
    @GetMapping("/reports")
    public String testReports(@RequestParam(value = "classId", required = false) Long classId,
                             @RequestParam(value = "startDate", required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                             @RequestParam(value = "endDate", required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                             Model model) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        List<SchoolClass> activeClasses = schoolClassService.getActiveClasses();
        model.addAttribute("classes", activeClasses);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        List<Test> tests;
        if (classId != null) {
            tests = testService.getClassTestsInPeriod(classId, startDate, endDate);
        } else {
            tests = testService.getTestsInPeriod(startDate, endDate);
        }
        
        List<TestScoreService.TestStatistics> testStatistics = tests.stream()
            .map(testScoreService::getTestStatistics)
            .collect(Collectors.toList());
        
        model.addAttribute("tests", tests);
        model.addAttribute("testStatistics", testStatistics);
        
        return "tests/reports";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateTest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Test test = testService.getTestById(id)
                .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
            
            testService.deactivateTest(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "テスト「" + test.getTestName() + "」を無効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/tests";
    }
    
    @PostMapping("/{id}/reactivate")
    public String reactivateTest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Test test = testService.getTestById(id)
                .orElseThrow(() -> new RuntimeException("テストが見つかりません"));
            
            testService.reactivateTest(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "テスト「" + test.getTestName() + "」を再有効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/tests/" + id;
    }
}