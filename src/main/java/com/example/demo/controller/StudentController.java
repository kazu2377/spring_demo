package com.example.demo.controller;

import com.example.demo.entity.Student;
import com.example.demo.entity.SchoolClass;
import com.example.demo.service.StudentService;
import com.example.demo.service.SchoolClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/students")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private SchoolClassService schoolClassService;
    
    @GetMapping
    public String listStudents(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "classId", required = false) Long classId,
                              Model model) {
        List<Student> students;
        
        if (search != null && !search.trim().isEmpty()) {
            students = studentService.searchStudentsByName(search.trim());
        } else if (classId != null) {
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            students = studentService.getStudentsByClass(schoolClass);
        } else {
            students = studentService.getActiveStudents();
        }
        
        model.addAttribute("students", students);
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        model.addAttribute("search", search);
        model.addAttribute("selectedClassId", classId);
        
        return "students/list";
    }
    
    @GetMapping("/{id}")
    public String viewStudent(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id)
            .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
        
        model.addAttribute("student", student);
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        return "students/view";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        model.addAttribute("suggestedStudentNumber", studentService.generateStudentNumber());
        return "students/create";
    }
    
    @PostMapping
    public String createStudent(@ModelAttribute Student student,
                               @RequestParam String username,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            if (student.getEnrollmentDate() == null) {
                student.setEnrollmentDate(LocalDate.now());
            }
            
            Student savedStudent = studentService.createStudent(student, username, password);
            redirectAttributes.addFlashAttribute("successMessage", 
                "受講生「" + savedStudent.getFullName() + "」を作成しました");
            return "redirect:/students/" + savedStudent.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/students/new";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id)
            .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
        
        model.addAttribute("student", student);
        model.addAttribute("classes", schoolClassService.getActiveClasses());
        return "students/edit";
    }
    
    @PostMapping("/{id}")
    public String updateStudent(@PathVariable Long id,
                               @ModelAttribute Student student,
                               RedirectAttributes redirectAttributes) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            redirectAttributes.addFlashAttribute("successMessage", 
                "受講生「" + updatedStudent.getFullName() + "」を更新しました");
            return "redirect:/students/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/students/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(id)
                .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
            
            studentService.deactivateStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "受講生「" + student.getFullName() + "」を無効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/students";
    }
    
    @PostMapping("/{id}/reactivate")
    public String reactivateStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(id)
                .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
            
            studentService.reactivateStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "受講生「" + student.getFullName() + "」を再有効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/students/" + id;
    }
    
    @PostMapping("/{id}/assign-class")
    public String assignToClass(@PathVariable Long id,
                               @RequestParam Long classId,
                               RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(id)
                .orElseThrow(() -> new RuntimeException("受講生が見つかりません"));
            
            SchoolClass schoolClass = schoolClassService.getClassById(classId)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            if (!schoolClassService.canAddStudent(classId)) {
                throw new RuntimeException("クラスの定員に達しています");
            }
            
            studentService.assignToClass(id, schoolClass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "受講生「" + student.getFullName() + "」をクラス「" + schoolClass.getClassName() + "」に割り当てました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/students/" + id;
    }
    
    @GetMapping("/check-student-number")
    @ResponseBody
    public boolean checkStudentNumber(@RequestParam String studentNumber) {
        return studentService.isStudentNumberAvailable(studentNumber);
    }
    
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return studentService.isEmailAvailable(email);
    }
}