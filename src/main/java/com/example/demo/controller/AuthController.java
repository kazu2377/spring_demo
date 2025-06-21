package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "ユーザー名またはパスワードが間違っています。");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "正常にログアウトしました。");
        }
        
        return "login";
    }
}