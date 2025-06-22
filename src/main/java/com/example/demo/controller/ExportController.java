package com.example.demo.controller;

import com.example.demo.entity.SchoolClass;
import com.example.demo.entity.Test;
import com.example.demo.repository.SchoolClassRepository;
import com.example.demo.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/export")
@PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
public class ExportController {

    @Autowired
    private SchoolClassRepository schoolClassRepository;
    
    @Autowired
    private TestRepository testRepository;

    @GetMapping
    public String showExportPage(Model model) {
        List<SchoolClass> classes = schoolClassRepository.findByActiveTrue();
        List<Test> tests = testRepository.findByActiveTrue();
        
        model.addAttribute("classes", classes);
        model.addAttribute("tests", tests);
        model.addAttribute("pageTitle", "データエクスポート");
        
        return "admin/export";
    }
}