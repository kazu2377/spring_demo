package com.example.demo.controller;

import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.Student;
import com.example.demo.entity.User;
import com.example.demo.entity.Role;
import com.example.demo.service.SchoolClassService;
import com.example.demo.service.StudentService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/classes")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class SchoolClassController {
    
    @Autowired
    private SchoolClassService schoolClassService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listClasses(@RequestParam(value = "search", required = false) String search,
                             Model model) {
        List<SchoolClass> classes;
        
        if (search != null && !search.trim().isEmpty()) {
            classes = schoolClassService.searchClasses(search.trim());
        } else {
            classes = schoolClassService.getActiveClasses();
        }
        
        model.addAttribute("classes", classes);
        model.addAttribute("search", search);
        
        return "classes/list";
    }
    
    @GetMapping("/{id}")
    public String viewClass(@PathVariable Long id, Model model) {
        SchoolClass schoolClass = schoolClassService.getClassById(id)
            .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
        
        List<Student> students = studentService.getStudentsByClass(schoolClass);
        
        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("students", students);
        
        return "classes/view";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<User> teachers = userService.findByRole(Role.TEACHER);
        teachers.addAll(userService.findByRole(Role.ADMIN));
        
        model.addAttribute("schoolClass", new SchoolClass());
        model.addAttribute("teachers", teachers);
        
        return "classes/create";
    }
    
    @PostMapping
    public String createClass(@ModelAttribute SchoolClass schoolClass,
                             RedirectAttributes redirectAttributes) {
        try {
            SchoolClass savedClass = schoolClassService.createClass(schoolClass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "クラス「" + savedClass.getClassName() + "」を作成しました");
            return "redirect:/classes/" + savedClass.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/classes/new";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        SchoolClass schoolClass = schoolClassService.getClassById(id)
            .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
        
        List<User> teachers = userService.findByRole(Role.TEACHER);
        teachers.addAll(userService.findByRole(Role.ADMIN));
        
        model.addAttribute("schoolClass", schoolClass);
        model.addAttribute("teachers", teachers);
        
        return "classes/edit";
    }
    
    @PostMapping("/{id}")
    public String updateClass(@PathVariable Long id,
                             @ModelAttribute SchoolClass schoolClass,
                             RedirectAttributes redirectAttributes) {
        try {
            SchoolClass updatedClass = schoolClassService.updateClass(id, schoolClass);
            redirectAttributes.addFlashAttribute("successMessage", 
                "クラス「" + updatedClass.getClassName() + "」を更新しました");
            return "redirect:/classes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
            return "redirect:/classes/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SchoolClass schoolClass = schoolClassService.getClassById(id)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            schoolClassService.deactivateClass(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "クラス「" + schoolClass.getClassName() + "」を無効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/classes";
    }
    
    @PostMapping("/{id}/reactivate")
    public String reactivateClass(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            SchoolClass schoolClass = schoolClassService.getClassById(id)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            schoolClassService.reactivateClass(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "クラス「" + schoolClass.getClassName() + "」を再有効化しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/classes/" + id;
    }
    
    @PostMapping("/{id}/assign-teacher")
    public String assignTeacher(@PathVariable Long id,
                               @RequestParam Long teacherId,
                               RedirectAttributes redirectAttributes) {
        try {
            SchoolClass schoolClass = schoolClassService.getClassById(id)
                .orElseThrow(() -> new RuntimeException("クラスが見つかりません"));
            
            User teacher = userService.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("講師が見つかりません"));
            
            schoolClassService.assignTeacher(id, teacher);
            redirectAttributes.addFlashAttribute("successMessage", 
                "講師「" + teacher.getUsername() + "」をクラス「" + schoolClass.getClassName() + "」に割り当てました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラー: " + e.getMessage());
        }
        return "redirect:/classes/" + id;
    }
    
    @GetMapping("/current")
    public String currentClasses(Model model) {
        List<SchoolClass> currentClasses = schoolClassService.getCurrentActiveClasses();
        model.addAttribute("classes", currentClasses);
        return "classes/current";
    }
    
    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkClassName(@RequestParam String className) {
        return schoolClassService.isClassNameAvailable(className);
    }
    
    @GetMapping("/{id}/can-add-student")
    @ResponseBody
    public boolean canAddStudent(@PathVariable Long id) {
        return schoolClassService.canAddStudent(id);
    }
}