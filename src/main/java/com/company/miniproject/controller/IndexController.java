package com.company.miniproject.controller;

import com.company.miniproject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public String index(Authentication authentication, Model model) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        boolean isManager = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        
        boolean isEmployee = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        Map<String, Long> stats = dashboardService.getStatistics(authentication);
        model.addAttribute("stats", stats);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        model.addAttribute("isEmployee", isEmployee);
        
        if (isAdmin) {
            Map<String, Object> adminData = dashboardService.getAdminDashboardData();
            model.addAttribute("dashboardData", adminData);
        } else if (isManager && !isAdmin) {
            Map<String, Object> managerData = dashboardService.getManagerDashboardData(authentication);
            model.addAttribute("dashboardData", managerData);
        } else if (isEmployee) {
            Map<String, Object> employeeData = dashboardService.getEmployeeDashboardData(authentication);
            model.addAttribute("dashboardData", employeeData);
        }
        
        return "index";
    }
    
    @GetMapping("/index")
    @PreAuthorize("isAuthenticated()")
    public String indexPage(Authentication authentication, Model model) {
        return index(authentication, model);
    }
}