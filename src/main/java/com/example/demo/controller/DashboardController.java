package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"));
        boolean isStudent = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"));
        
        // 役割別のダッシュボードにリダイレクト
        if (isStudent) {
            return "redirect:/student/dashboard";
        } else if (isAdmin || isTeacher) {
            // 管理者・講師は職業訓練校管理ダッシュボード
            model.addAttribute("username", username);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isTeacher", isTeacher);
            return "school-dashboard";
        } else {
            // 従来の一般ユーザー用ダッシュボード
            model.addAttribute("username", username);
            model.addAttribute("isAdmin", isAdmin);
            return "dashboard";
        }
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}