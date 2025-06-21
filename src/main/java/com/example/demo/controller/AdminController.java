package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String adminHome() {
        return "admin/index";
    }
    
    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }
    
    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam Role role,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, password, role);
            redirectAttributes.addFlashAttribute("successMessage", "ユーザーが正常に作成されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーの作成に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/user-edit";
    }
    
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
                           @RequestParam Role role,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "ユーザーが正常に更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーの更新に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "ユーザーが正常に削除されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーの削除に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}