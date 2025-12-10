package com.company.miniproject.controller;

import com.company.miniproject.entity.Department;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String listDepartments(Model model) {
        try {
            List<Department> departments = departmentService.findAll();
            model.addAttribute("departments", departments);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading departments: " + e.getMessage());
        }
        return "department/list";
    }
    
    @GetMapping("/test")
    @Transactional(readOnly = true)
    public String testDepartments(Model model) {
        List<Department> departments = departmentService.findAll();
        model.addAttribute("departments", departments);
        return "department/test";
    }
    
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showDepartmentDetail(@PathVariable Integer id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        model.addAttribute("department", department);
        
        // Get employees not in this department for the add employee form
        List<com.company.miniproject.entity.Employee> availableEmployees = 
            employeeRepository.findEmployeesNotInDepartment(id);
        model.addAttribute("availableEmployees", availableEmployees);
        
        return "department/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("formAction", "/departments"); // Set form action for new department
        return "department/form";
    }

    @PostMapping
    public String createDepartment(@Valid @ModelAttribute("department") Department department,
                                  BindingResult result,
                                  Model model, // Added Model for error re-rendering
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/departments"); // Keep form action on error
            return "department/form";
        }
        
        try {
            departmentService.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/departments"); // Keep form action on error
            return "department/form";
        }
        
        return "redirect:/departments";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        model.addAttribute("department", department);
        model.addAttribute("formAction", "/departments/" + id); // Set form action for edit department
        return "department/form";
    }

    @PostMapping("/{id}")
    public String updateDepartment(@PathVariable Integer id,
                                   @Valid @ModelAttribute("department") Department department,
                                   BindingResult result,
                                   Model model, // Added Model for error re-rendering
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/departments/" + id); // Keep form action on error
            return "department/form";
        }
        
        try {
            departmentService.update(id, department);
            redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/departments/" + id); // Keep form action on error
            return "department/form";
        }
        
        return "redirect:/departments";
    }

    @PostMapping("/{id}/delete")
    public String deleteDepartment(@PathVariable Integer id,
                                   RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/departments";
    }
    
    @PostMapping("/{id}/add-employee")
    public String addEmployeeToDepartment(@PathVariable Integer id,
                                         @RequestParam Integer employeeId,
                                         RedirectAttributes redirectAttributes) {
        try {
            departmentService.addEmployeeToDepartment(id, employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Employee added to department successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/departments/" + id;
    }
    
    @PostMapping("/{id}/remove-employee/{employeeId}")
    public String removeEmployeeFromDepartment(@PathVariable Integer id,
                                               @PathVariable Integer employeeId,
                                               RedirectAttributes redirectAttributes) {
        try {
            departmentService.removeEmployeeFromDepartment(id, employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Employee removed from department successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/departments/" + id;
    }
}
