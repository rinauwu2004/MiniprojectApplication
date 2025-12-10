package com.company.miniproject.controller;

import com.company.miniproject.entity.Project;
import com.company.miniproject.entity.ProjectStatus;
import com.company.miniproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/projects")
    @Transactional(readOnly = true)
    public String listProjects(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectService.findAll(pageable);
        
        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("totalItems", projectPage.getTotalElements());
        model.addAttribute("statuses", ProjectStatus.values());
        
        return "project/list";
    }
    
    @GetMapping("/employees")
    public String listEmployees() {
        // Redirect to employees list (same as admin)
        return "redirect:/employees";
    }
    
    @GetMapping("/departments")
    public String listDepartments() {
        // Redirect to departments list (view only for manager)
        return "redirect:/departments";
    }
}

