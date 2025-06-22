package com.example.demo.controller;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.AttendanceStatus;
import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.User;
import com.example.demo.service.AttendanceService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SchoolClassService schoolClassService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listAttendance(@RequestParam(value = "classId", required = false) Long classId,
                                @RequestParam(value = "date", required = false) 
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                Model model) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        List<SchoolClass> activeClasses = schoolClassService.getActiveClasses();
        model.addAttribute("classes", activeClasses);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedClassId", classId);
        
        if (classId != null) {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            List<Student> students = studentService.getStudentsByClass(schoolClass);
            List<Attendance> attendanceRecords = attendanceService.getClassAttendanceOnDate(classId, date);
            
            Map<Long, Attendance> attendanceMap = attendanceRecords.stream()
                .collect(Collectors.toMap(
                    a -> a.getStudent().getId(),
                    a -> a
                ));
            
            model.addAttribute("schoolClass", schoolClass);
            model.addAttribute("students", students);
            model.addAttribute("attendanceMap", attendanceMap);
            model.addAttribute("attendanceStatuses", AttendanceStatus.values());
        }
        
        return "attendance/list";
    }
    
    @PostMapping("/record")
    public String recordAttendance(@RequestParam Long classId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam Map<String, String> formParams,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            int recordedCount = 0;
            
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                if (key.startsWith("attendance_") && !value.isEmpty()) {
                    Long studentId = Long.valueOf(key.substring("attendance_".length()));
                    Student student = studentService.getStudentById(studentId)
                        .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
                    
                    AttendanceStatus status = AttendanceStatus.valueOf(value);
                    String notes = formParams.getOrDefault("notes_" + studentId, "");
                    
                    attendanceService.recordAttendance(student, schoolClass, date, status, notes, currentUser);
                    recordedCount++;
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                recordedCount + "件の出席記録を保存しました");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        
        return "redirect:/attendance?classId=" + classId + "&date=" + date;
    }
    
    @PostMapping("/bulk-create")
    public String createBulkAttendance(@RequestParam Long classId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                      @RequestParam AttendanceStatus defaultStatus,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            List<Student> students = studentService.getStudentsByClass(schoolClass);
            
            User currentUser = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            attendanceService.createBulkAttendance(students, schoolClass, date, defaultStatus, currentUser);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "全受講生の出席記録を「" + defaultStatus.getDisplayName() + "」で作成しました");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        
        return "redirect:/attendance?classId=" + classId + "&date=" + date;
    }
    
    @GetMapping("/student/{studentId}")
    public String viewStudentAttendance(@PathVariable Long studentId,
                                       @RequestParam(value = "startDate", required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam(value = "endDate", required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       Model model) {
        
        Student student = studentService.getStudentById(studentId)
            .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<Attendance> attendanceRecords = attendanceService.getStudentAttendanceInPeriod(studentId, startDate, endDate);
        AttendanceService.AttendanceSummary summary = attendanceService.getAttendanceSummary(student);
        
        model.addAttribute("student", student);
        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("attendanceSummary", summary);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        return "attendance/student-view";
    }
    
    @GetMapping("/reports")
    public String attendanceReports(@RequestParam(value = "classId", required = false) Long classId,
                                   @RequestParam(value = "startDate", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(value = "endDate", required = false)
                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   Model model) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        List<SchoolClass> activeClasses = schoolClassService.getActiveClasses();
        model.addAttribute("classes", activeClasses);
        model.addAttribute("selectedClassId", classId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        if (classId != null) {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            List<Student> students = studentService.getStudentsByClass(schoolClass);
            List<AttendanceService.AttendanceSummary> summaries = students.stream()
                .map(attendanceService::getAttendanceSummary)
                .collect(Collectors.toList());
            
            model.addAttribute("schoolClass", schoolClass);
            model.addAttribute("students", students);
            model.addAttribute("attendanceSummaries", summaries);
        }
        
        return "attendance/reports";
    }
    
    @DeleteMapping("/{id}")
    public String deleteAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.deleteAttendance(id);
            redirectAttributes.addFlashAttribute("successMessage", "出席記録を削除しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/attendance";
    }
}