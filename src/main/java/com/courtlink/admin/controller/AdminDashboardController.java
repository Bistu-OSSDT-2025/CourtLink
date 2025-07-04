package com.courtlink.admin.controller;

import com.courtlink.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api/admin")
public class AdminDashboardController {
    
    @Autowired
    private AdminService adminService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get admin statistics
        Map<String, Object> stats = adminService.getAdminStatistics();
        model.addAttribute("stats", stats);
        
        return "admin-dashboard";
    }
} 
