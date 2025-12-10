package com.company.miniproject.controller;

import com.company.miniproject.dto.ProjectAssignmentDto;
import com.company.miniproject.entity.*;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String listProjects(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model,
                              Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Project> projectPage;
            
            // Check if user is EMPLOYEE (and not ADMIN/MANAGER) - only show assigned projects
            boolean isEmployeeOnly = authentication != null && 
                    authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")) &&
                    !authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                                         a.getAuthority().equals("ROLE_MANAGER"));
            
            if (isEmployeeOnly) {
                // Find employee by account
                String username = authentication.getName();
                Optional<Account> accountOpt = accountRepository.findByUsername(username);
                if (accountOpt.isPresent()) {
                    Optional<Employee> employeeOpt = employeeRepository.findByAccountId(accountOpt.get().getId());
                    if (employeeOpt.isPresent()) {
                        projectPage = projectService.findByEmployeeId(employeeOpt.get().getId(), pageable);
                    } else {
                        // No employee record, show empty list
                        projectPage = new PageImpl<>(List.of(), pageable, 0);
                    }
                } else {
                    projectPage = new PageImpl<>(List.of(), pageable, 0);
                }
            } else {
                // ADMIN and MANAGER see all projects
                projectPage = projectService.findAll(pageable);
            }
            
            model.addAttribute("projects", projectPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", projectPage.getTotalPages());
            model.addAttribute("totalItems", projectPage.getTotalElements());
            model.addAttribute("statuses", ProjectStatus.values());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading projects: " + e.getMessage());
        }
        
        return "project/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("formAction", "/projects");
        return "project/form";
    }

    @PostMapping
    public String createProject(@Valid @ModelAttribute("project") Project project,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Custom validation: endDate must be after startDate
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEndDate().isBefore(project.getStartDate()) || 
                project.getEndDate().isEqual(project.getStartDate())) {
                result.rejectValue("endDate", "error.endDate", "End date must be after start date");
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("formAction", "/projects");
            return "project/form";
        }
        
        try {
            projectService.save(project);
            redirectAttributes.addFlashAttribute("successMessage", "Project created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("formAction", "/projects");
            return "project/form";
        }
        
        return "redirect:/projects";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showProjectDetail(@PathVariable Integer id, Model model,
                                   Authentication authentication) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        
        List<ProjectAssignment> assignments = projectService.getProjectAssignments(id);
        
        model.addAttribute("project", project);
        model.addAttribute("assignments", assignments);
        model.addAttribute("statuses", ProjectStatus.values());
        
        // Load employees list for ADMIN and MANAGER (for adding members) with department eagerly fetched
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
            List<Employee> allEmployees = employeeRepository.findAllWithDepartment();
            model.addAttribute("allEmployees", allEmployees);
            model.addAttribute("assignmentDto", new ProjectAssignmentDto());
        }
        
        return "project/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        model.addAttribute("project", project);
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("formAction", "/projects/" + id);
        return "project/form";
    }

    @PostMapping("/{id}")
    public String updateProject(@PathVariable Integer id,
                               @Valid @ModelAttribute("project") Project project,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Custom validation: endDate must be after startDate
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEndDate().isBefore(project.getStartDate()) || 
                project.getEndDate().isEqual(project.getStartDate())) {
                result.rejectValue("endDate", "error.endDate", "End date must be after start date");
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("formAction", "/projects/" + id);
            return "project/form";
        }
        
        try {
            projectService.update(id, project);
            redirectAttributes.addFlashAttribute("successMessage", "Project updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("formAction", "/projects/" + id);
            return "project/form";
        }
        
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Integer id,
                               RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/projects";
    }

    @PostMapping("/{id}/assignments")
    public String addProjectAssignment(@PathVariable Integer id,
                                     @Valid @ModelAttribute("assignmentDto") ProjectAssignmentDto dto,
                                     BindingResult result,
                                     Model model,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        // Set projectId from path variable (not from form)
        dto.setProjectId(id);
        
        // Always load project and assignments for the view
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        List<ProjectAssignment> assignments = projectService.getProjectAssignments(id);
        model.addAttribute("project", project);
        model.addAttribute("assignments", assignments);
        model.addAttribute("statuses", ProjectStatus.values());
        
        // Load employees list for ADMIN/MANAGER with department eagerly fetched
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
            List<Employee> allEmployees = employeeRepository.findAllWithDepartment();
            model.addAttribute("allEmployees", allEmployees);
        }
        
        if (result.hasErrors()) {
            // Keep the DTO with entered values and errors for display
            model.addAttribute("assignmentDto", dto);
            
            // Build detailed error message for flash attribute (if needed)
            StringBuilder errorMsg = new StringBuilder("Please fix the following errors: ");
            result.getFieldErrors().forEach(error -> {
                errorMsg.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append(". ");
            });
            model.addAttribute("errorMessage", errorMsg.toString().trim());
            
            // Return the view directly to show validation errors on the form
            return "project/detail";
        }
        
        dto.setProjectId(id);
        
        try {
            projectService.addProjectAssignment(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Employee assigned to project successfully");
        } catch (IllegalArgumentException e) {
            // If service throws exception, reload the form with error
            model.addAttribute("assignmentDto", new ProjectAssignmentDto()); // Reset form
            model.addAttribute("errorMessage", e.getMessage());
            return "project/detail";
        }
        
        return "redirect:/projects/" + id;
    }

    @PostMapping("/assignments/{assignmentId}/delete")
    public String removeProjectAssignment(@PathVariable Integer assignmentId,
                                         @RequestParam Integer projectId,
                                         RedirectAttributes redirectAttributes) {
        try {
            projectService.removeProjectAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment removed successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/projects/" + projectId;
    }
}
