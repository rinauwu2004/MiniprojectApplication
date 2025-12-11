package com.company.miniproject.controller;

import com.company.miniproject.dto.AccountDto;
import com.company.miniproject.dto.AdminChangePasswordDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.entity.Role;
import com.company.miniproject.repository.RoleRepository;
import com.company.miniproject.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String listAccounts(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer roleId,
                               @RequestParam(required = false) AccountStatus status,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String sortBy,
                               @RequestParam(required = false) String sortDir,
                               Model model) {
        try {
            String sortField = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "username";
            Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            if (!isValidSortField(sortField)) {
                sortField = "username";
                direction = Sort.Direction.ASC;
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<Account> accountPage;
            
            if ((keyword != null && !keyword.trim().isEmpty()) || roleId != null || status != null) {
                accountPage = accountService.searchAccounts(keyword, roleId, status, pageable);
            } else {
                accountPage = accountService.findAll(pageable);
            }
            
            model.addAttribute("accounts", accountPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", accountPage.getTotalPages());
            model.addAttribute("totalItems", accountPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("sortBy", sortField);
            model.addAttribute("sortDir", direction.toString().toLowerCase());
            model.addAttribute("keyword", keyword);
            model.addAttribute("roleId", roleId);
            model.addAttribute("status", status);
            model.addAttribute("statuses", AccountStatus.values());
            model.addAttribute("roles", roleRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading accounts: " + e.getMessage());
        }
        
        return "account/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("accountDto", new AccountDto());
        model.addAttribute("roles", roles);
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("formAction", "/accounts");
        return "account/form";
    }

    @PostMapping
    public String createAccount(@Valid @ModelAttribute("accountDto") AccountDto dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.password", "Password is required");
        }
        
        if (result.hasErrors()) {
            List<Role> roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            model.addAttribute("statuses", AccountStatus.values());
            model.addAttribute("formAction", "/accounts");
            return "account/form";
        }
        
        try {
            accountService.save(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully");
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("Username already exists")) {
                    result.rejectValue("username", "error.username.exists", errorMsg);
                } else if (errorMsg.contains("Email already exists")) {
                    result.rejectValue("email", "error.email.exists", errorMsg);
                } else {
                    model.addAttribute("errorMessage", errorMsg);
                }
            }
            model.addAttribute("accountDto", dto);
            List<Role> roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            model.addAttribute("statuses", AccountStatus.values());
            model.addAttribute("formAction", "/accounts");
            return "account/form";
        }
        
        return "redirect:/accounts";
    }

    @GetMapping("/{id}/edit")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        boolean isAdminAccount = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getEmail());
        dto.setStatus(account.getStatus());
        dto.setRoleIds(account.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet()));
        
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("accountDto", dto);
        model.addAttribute("roles", roles);
        model.addAttribute("statuses", AccountStatus.values());
        model.addAttribute("formAction", "/accounts/" + id);
        model.addAttribute("isAdminAccount", isAdminAccount);
        
        return "account/form";
    }

    @PostMapping("/{id}")
    public String updateAccount(@PathVariable Integer id,
                                @Valid @ModelAttribute("accountDto") AccountDto dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Account account = accountService.findById(id).orElse(null);
        boolean isAdminAccount = account != null && account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        
        if (isAdminAccount && account != null) {
            dto.setUsername(account.getUsername());
            dto.setEmail(account.getEmail());
            dto.setRoleIds(account.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet()));
        }
        
        if (result.hasErrors()) {
            List<Role> roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            model.addAttribute("statuses", AccountStatus.values());
            model.addAttribute("formAction", "/accounts/" + id);
            model.addAttribute("isAdminAccount", isAdminAccount);
            return "account/form";
        }
        
        try {
            accountService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully");
        } catch (IllegalArgumentException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("Username already exists")) {
                    result.rejectValue("username", "error.username.exists", errorMsg);
                } else if (errorMsg.contains("Email already exists")) {
                    result.rejectValue("email", "error.email.exists", errorMsg);
                } else {
                    model.addAttribute("errorMessage", errorMsg);
                }
            }
            model.addAttribute("accountDto", dto);
            model.addAttribute("isAdminAccount", isAdminAccount);
            List<Role> roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            model.addAttribute("statuses", AccountStatus.values());
            model.addAttribute("formAction", "/accounts/" + id);
            return "account/form";
        }
        
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/accounts";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showAccountDetail(@PathVariable Integer id, Model model) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        model.addAttribute("account", account);
        return "account/detail";
    }

    @GetMapping("/{id}/change-password")
    @Transactional(readOnly = true)
    public String showChangePasswordForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        boolean isAdminAccount = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        if (isAdminAccount) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot change password for admin accounts. Admin accounts can only be viewed.");
            return "redirect:/accounts/" + id;
        }
        
        model.addAttribute("account", account);
        model.addAttribute("adminChangePasswordDto", new AdminChangePasswordDto());
        return "account/change-password";
    }

    @PostMapping("/{id}/change-password")
    public String changePassword(@PathVariable Integer id,
                                 @Valid @ModelAttribute("adminChangePasswordDto") AdminChangePasswordDto dto,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        model.addAttribute("account", account);
        
        if (!result.hasFieldErrors("newPassword") && !result.hasFieldErrors("confirmPassword")) {
            if (dto.getNewPassword() != null && dto.getConfirmPassword() != null 
                    && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.confirmPassword", "New password and confirm password do not match");
            }
        }
        
        if (result.hasErrors()) {
            return "account/change-password";
        }
        
        try {
            accountService.adminChangePassword(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/accounts/" + id;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/" + id + "/change-password";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/" + id;
        }
    }
    
    private boolean isValidSortField(String field) {
        return field != null && (field.equals("username") || field.equals("email") || field.equals("status"));
    }
}

