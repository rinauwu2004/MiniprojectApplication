package com.company.miniproject.controller;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.entity.Gender;
import com.company.miniproject.service.DepartmentService;
import com.company.miniproject.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public String listEmployees(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer departmentId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String sortBy,
                               @RequestParam(required = false) String sortDir,
                               Model model) {
        try {
            String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "fullName";
            Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            if (!isValidSortField(sortField)) {
                sortField = "fullName";
                direction = Sort.Direction.ASC;
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<Employee> employeePage;
            
            if (keyword != null && !keyword.trim().isEmpty() || departmentId != null) {
                employeePage = employeeService.searchEmployees(keyword, departmentId, pageable);
            } else {
                employeePage = employeeService.findAll(pageable);
            }
            
            List<Department> departments = departmentService.findAll();
            
            model.addAttribute("employees", employeePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", employeePage.getTotalPages());
            model.addAttribute("totalItems", employeePage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortField);
            model.addAttribute("sortDir", direction.toString().toLowerCase());
            model.addAttribute("keyword", keyword);
            model.addAttribute("departmentId", departmentId);
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading employees: " + e.getMessage());
        }
        
        return "employee/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<Department> departments = departmentService.findAll();
        model.addAttribute("employeeDto", new EmployeeRegistrationDto());
        model.addAttribute("departments", departments);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("formAction", "/employees");
        return "employee/form";
    }

    @PostMapping
    public String createEmployee(@Valid @ModelAttribute("employeeDto") EmployeeRegistrationDto dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.password", "Password is required");
        }
        
        if (result.hasErrors()) {
            List<Department> departments = departmentService.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("formAction", "/employees");
            return "employee/form";
        }
        
        try {
            employeeService.save(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Employee created successfully");
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("Username already exists")) {
                    result.rejectValue("username", "error.username.exists", errorMsg);
                } else if (errorMsg.contains("Email already exists")) {
                    result.rejectValue("email", "error.email.exists", errorMsg);
                } else if (errorMsg.contains("Phone number already exists") || errorMsg.contains("Phone already exists")) {
                    result.rejectValue("phone", "error.phone.exists", errorMsg);
                } else {
                    model.addAttribute("errorMessage", errorMsg);
                }
            }
            model.addAttribute("employeeDto", dto);
            List<Department> departments = departmentService.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("formAction", "/employees");
            return "employee/form";
        }
        
        return "redirect:/employees";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        EmployeeRegistrationDto dto = new EmployeeRegistrationDto();
        dto.setFullName(employee.getFullName());
        dto.setBirthDate(employee.getBirthDate());
        model.addAttribute("formAction", "/employees/" + id);
        dto.setGender(employee.getGender());
        dto.setPhone(employee.getPhone());
        dto.setAddress(employee.getAddress());
        dto.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        dto.setUsername(employee.getAccount().getUsername());
        dto.setEmail(employee.getAccount().getEmail());
        
        List<Department> departments = departmentService.findAll();
        model.addAttribute("employeeDto", dto);
        model.addAttribute("employeeId", id);
        model.addAttribute("departments", departments);
        model.addAttribute("genders", Gender.values());
        
        return "employee/form";
    }

    @PostMapping("/{id}")
    public String updateEmployee(@PathVariable Integer id,
                                @Valid @ModelAttribute("employeeDto") EmployeeRegistrationDto dto,
                                BindingResult result,
                                Model model,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
            dto.setUsername(employee.getAccount().getUsername());
            dto.setEmail(employee.getAccount().getEmail());
        }
        
        if (result.hasErrors()) {
            List<Department> departments = departmentService.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("employeeId", id);
            model.addAttribute("formAction", "/employees/" + id);
            return "employee/form";
        }
        
        try {
            employeeService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully");
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("Username already exists")) {
                    result.rejectValue("username", "error.username.exists", errorMsg);
                } else if (errorMsg.contains("Email already exists")) {
                    result.rejectValue("email", "error.email.exists", errorMsg);
                } else if (errorMsg.contains("Phone number already exists") || errorMsg.contains("Phone already exists")) {
                    result.rejectValue("phone", "error.phone.exists", errorMsg);
                } else {
                    model.addAttribute("errorMessage", errorMsg);
                }
            }
            model.addAttribute("employeeDto", dto);
            List<Department> departments = departmentService.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("employeeId", id);
            model.addAttribute("formAction", "/employees/" + id);
            return "employee/form";
        }
        
        return "redirect:/employees";
    }

    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/employees";
    }

    @GetMapping("/{id}/change-password")
    public String showChangePasswordForm(@PathVariable Integer id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        model.addAttribute("employee", employee);
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "employee/change-password";
    }

    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Integer id,
                                 @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        if (!result.hasFieldErrors("newPassword") && !result.hasFieldErrors("confirmPassword")) {
            if (dto.getNewPassword() != null && dto.getConfirmPassword() != null 
                    && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword", "New password and confirm password do not match");
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("employee", employee);
            return "employee/change-password";
        }
        
        try {
            employeeService.changePassword(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully. Please login again.");
            return "redirect:/logout";
        } catch (IllegalArgumentException e) {
            result.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            model.addAttribute("employee", employee);
            return "employee/change-password";
        }
    }
    
    private boolean isValidSortField(String field) {
        return field != null && (field.equals("fullName") || field.equals("phone") 
                || field.equals("gender") || field.equals("account.email") || field.equals("department.name"));
    }
}
