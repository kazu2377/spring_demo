package com.example.demo.controller;

import com.example.demo.service.DataInitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/data")
@PreAuthorize("hasRole('ADMIN')")
public class DataInitializationController {

    @Autowired
    private DataInitializationService dataInitializationService;

    @GetMapping
    public String showDataManagement(Model model) {
        model.addAttribute("pageTitle", "データ管理");
        return "admin/data-management";
    }

    @PostMapping("/initialize")
    public String initializeSampleData(RedirectAttributes redirectAttributes) {
        try {
            dataInitializationService.initializeSampleData();
            redirectAttributes.addFlashAttribute("successMessage", 
                "サンプルデータの初期化が完了しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "データ初期化中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/admin/data";
    }

    @PostMapping("/clear")
    public String clearAllData(RedirectAttributes redirectAttributes) {
        try {
            dataInitializationService.clearAllData();
            redirectAttributes.addFlashAttribute("successMessage", 
                "全データのクリアが完了しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "データクリア中にエラーが発生しました: " + e.getMessage());
        }
        return "redirect:/admin/data";
    }
}