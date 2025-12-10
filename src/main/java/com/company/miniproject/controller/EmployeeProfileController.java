package com.company.miniproject.controller;

import com.company.miniproject.entity.Employee;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.repository.ProjectAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeProfileController {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        
        // Find account by username
        var account = accountRepository.findByUsername(username);
        if (account.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "employee/profile";
        }
        
        // Find employee by account
        var employee = employeeRepository.findByAccountId(account.get().getId());
        if (employee.isPresent()) {
            Employee emp = employee.get();
            model.addAttribute("employee", emp);
            
            // Get projects assigned to this employee
            try {
                List<com.company.miniproject.entity.ProjectAssignment> assignments = 
                    projectAssignmentRepository.findByEmployeeId(emp.getId());
                model.addAttribute("assignments", assignments);
            } catch (Exception e) {
                // If error loading assignments, just show empty list
                model.addAttribute("assignments", Collections.emptyList());
            }
        } else {
            // Account exists but no employee record (e.g., Manager without employee record)
            model.addAttribute("errorMessage", "Employee profile not found. This account may not have an employee record.");
            model.addAttribute("assignments", Collections.emptyList());
        }
        
        return "employee/profile";
    }
    
    @GetMapping("/projects")
    @Transactional(readOnly = true)
    public String showMyProjects(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        
        // Find account by username
        var account = accountRepository.findByUsername(username);
        if (account.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "employee/projects";
        }
        
        // Find employee by account
        var employee = employeeRepository.findByAccountId(account.get().getId());
        if (employee.isPresent()) {
            // Get projects assigned to this employee
            try {
                List<com.company.miniproject.entity.ProjectAssignment> assignments = 
                    projectAssignmentRepository.findByEmployeeId(employee.get().getId());
                model.addAttribute("assignments", assignments);
            } catch (Exception e) {
                // If error loading assignments, just show empty list
                model.addAttribute("assignments", Collections.emptyList());
                model.addAttribute("errorMessage", "Error loading project assignments");
            }
        } else {
            model.addAttribute("errorMessage", "Employee profile not found. This account may not have an employee record.");
            model.addAttribute("assignments", Collections.emptyList());
        }
        
        return "employee/projects";
    }
}

