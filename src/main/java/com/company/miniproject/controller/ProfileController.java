package com.company.miniproject.controller;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.entity.Gender;
import com.company.miniproject.entity.ProjectAssignment;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.DepartmentRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.repository.ProjectAssignmentRepository;
import com.company.miniproject.service.AccountService;
import com.company.miniproject.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "profile/view";
        }
        
        Account account = accountOpt.get();
        if (account.getRoles() != null) {
            account.getRoles().size();
        }
        if (account.getEmployee() != null) {
            account.getEmployee().getId();
        }
        model.addAttribute("account", account);
        
        Optional<Employee> employeeOpt = employeeRepository.findByAccountId(account.getId());
        if (employeeOpt.isPresent()) {
            model.addAttribute("employee", employeeOpt.get());
        }
        
        return "profile/view";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordForm(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "redirect:/profile";
        }
        
        model.addAttribute("account", accountOpt.get());
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        return "profile/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
            return "redirect:/profile";
        }
        
        Account account = accountOpt.get();
        model.addAttribute("account", account);
        
        if (!result.hasFieldErrors("newPassword") && !result.hasFieldErrors("confirmPassword")) {
            if (dto.getNewPassword() != null && dto.getConfirmPassword() != null 
                    && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword", "New password and confirm password do not match");
            }
        }
        
        if (result.hasErrors()) {
            return "profile/change-password";
        }
        
        try {
            accountService.changePassword(account.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully. Please login again.");
            return "redirect:/logout";
        } catch (IllegalArgumentException e) {
            result.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            return "profile/change-password";
        }
    }
    
    @GetMapping("/profile/edit")
    @Transactional(readOnly = true)
    public String showEditProfileForm(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "redirect:/profile";
        }
        
        Account account = accountOpt.get();
        Optional<Employee> employeeOpt = employeeRepository.findByAccountId(account.getId());
        if (employeeOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Employee profile not found");
            return "redirect:/profile";
        }
        
        Employee employee = employeeOpt.get();
        EmployeeRegistrationDto dto = new EmployeeRegistrationDto();
        dto.setFullName(employee.getFullName());
        dto.setBirthDate(employee.getBirthDate());
        dto.setGender(employee.getGender());
        dto.setPhone(employee.getPhone());
        dto.setAddress(employee.getAddress());
        dto.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getEmail());
        
        List<Department> departments = departmentRepository.findAll();
        model.addAttribute("employeeDto", dto);
        model.addAttribute("employeeId", employee.getId());
        model.addAttribute("departments", departments);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("formAction", "/profile/update");
        
        return "profile/edit";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(Authentication authentication,
                               @Valid @ModelAttribute("employeeDto") EmployeeRegistrationDto dto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found");
            return "redirect:/profile";
        }
        
        Account account = accountOpt.get();
        Optional<Employee> employeeOpt = employeeRepository.findByAccountId(account.getId());
        if (employeeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee profile not found");
            return "redirect:/profile";
        }
        
        Employee employee = employeeOpt.get();
        
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getEmail());
        dto.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        
        if (result.hasErrors()) {
            List<Department> departments = departmentRepository.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("formAction", "/profile/update");
            return "profile/edit";
        }
        
        try {
            employeeService.update(employee.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
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
            List<Department> departments = departmentRepository.findAll();
            model.addAttribute("departments", departments);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("formAction", "/profile/update");
            return "profile/edit";
        }
        
        return "redirect:/profile";
    }
    
    @GetMapping("/employee/profile")
    @Transactional(readOnly = true)
    public String showEmployeeProfile(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        var account = accountRepository.findByUsername(username);
        if (account.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "employee/profile";
        }
        
        var employee = employeeRepository.findByAccountId(account.get().getId());
        if (employee.isPresent()) {
            Employee emp = employee.get();
            model.addAttribute("employee", emp);
            
            try {
                List<ProjectAssignment> assignments = 
                    projectAssignmentRepository.findByEmployeeId(emp.getId());
                model.addAttribute("assignments", assignments);
            } catch (Exception e) {
                model.addAttribute("assignments", Collections.emptyList());
            }
        } else {
            model.addAttribute("errorMessage", "Employee profile not found. This account may not have an employee record.");
            model.addAttribute("assignments", Collections.emptyList());
        }
        
        return "employee/profile";
    }
    
    @GetMapping("/employee/projects")
    @Transactional(readOnly = true)
    public String showMyProjects(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        var account = accountRepository.findByUsername(username);
        if (account.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found");
            return "employee/projects";
        }
        
        var employee = employeeRepository.findByAccountId(account.get().getId());
        if (employee.isPresent()) {
            try {
                List<ProjectAssignment> assignments = 
                    projectAssignmentRepository.findByEmployeeId(employee.get().getId());
                model.addAttribute("assignments", assignments);
            } catch (Exception e) {
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

