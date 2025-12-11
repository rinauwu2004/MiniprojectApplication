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
    public String listDepartments(@RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) String sortDir,
                                 Model model) {
        try {
            String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "name";
            boolean ascending = (sortDir == null || !sortDir.equalsIgnoreCase("desc"));
            
            if (!isValidSortField(sortField)) {
                sortField = "name";
                ascending = true;
            }
            
            List<Department> departments = departmentService.findAll(sortField, ascending);
            model.addAttribute("departments", departments);
            model.addAttribute("sortBy", sortField);
            model.addAttribute("sortDir", ascending ? "asc" : "desc");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading departments: " + e.getMessage());
        }
        return "department/list";
    }
    
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showDepartmentDetail(@PathVariable Integer id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        model.addAttribute("department", department);
        
        List<com.company.miniproject.entity.Employee> availableEmployees = 
            employeeRepository.findEmployeesNotInDepartment(id);
        model.addAttribute("availableEmployees", availableEmployees);
        
        return "department/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("formAction", "/departments");
        return "department/form";
    }

    @PostMapping
    public String createDepartment(@Valid @ModelAttribute("department") Department department,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/departments");
            return "department/form";
        }
        
        try {
            departmentService.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/departments");
            return "department/form";
        }
        
        return "redirect:/departments";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Department department = departmentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        model.addAttribute("department", department);
        model.addAttribute("formAction", "/departments/" + id);
        return "department/form";
    }

    @PostMapping("/{id}")
    public String updateDepartment(@PathVariable Integer id,
                                   @Valid @ModelAttribute("department") Department department,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/departments/" + id);
            return "department/form";
        }
        
        try {
            departmentService.update(id, department);
            redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/departments/" + id);
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
    
    private boolean isValidSortField(String field) {
        return field != null && (field.equals("name") || field.equals("description"));
    }
}
