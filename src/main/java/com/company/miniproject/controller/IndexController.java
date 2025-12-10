package com.company.miniproject.controller;

import com.company.miniproject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String index(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
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
        
        return "index";
    }
    
    @GetMapping("/index")
    public String indexPage(Authentication authentication, Model model) {
        return index(authentication, model);
    }
}