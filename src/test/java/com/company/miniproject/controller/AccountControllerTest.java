package com.company.miniproject.controller;

import com.company.miniproject.dto.AccountDto;
import com.company.miniproject.dto.AdminChangePasswordDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.entity.Role;
import com.company.miniproject.repository.RoleRepository;
import com.company.miniproject.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AccountController accountController;

    private Account account;
    private AccountDto accountDto;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ADMIN");

        account = new Account();
        account.setId(1);
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setStatus(AccountStatus.Active);
        account.setRoles(new HashSet<>(Collections.singletonList(role)));

        accountDto = new AccountDto();
        accountDto.setUsername("newuser");
        accountDto.setEmail("new@example.com");
        accountDto.setPassword("password123");
        accountDto.setStatus(AccountStatus.Active);
        accountDto.setRoleIds(new HashSet<>(Collections.singletonList(1)));
    }

    @Test
    void testListAccounts_ShouldReturnList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Arrays.asList(account));
        when(accountService.findAll(pageable)).thenReturn(page);
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        String result = accountController.listAccounts(null, null, null, 0, 10, model);

        assertEquals("account/list", result);
        verify(model).addAttribute("accounts", page.getContent());
    }

    @Test
    void testListAccounts_WithFilters_ShouldUseSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Arrays.asList(account));
        when(accountService.searchAccounts("test", 1, AccountStatus.Active, pageable)).thenReturn(page);
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        String result = accountController.listAccounts("test", 1, AccountStatus.Active, 0, 10, model);

        assertEquals("account/list", result);
        verify(accountService).searchAccounts("test", 1, AccountStatus.Active, pageable);
    }

    @Test
    void testShowCreateForm_ShouldReturnForm() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        String result = accountController.showCreateForm(model);

        assertEquals("account/form", result);
        verify(model).addAttribute(eq("accountDto"), any(AccountDto.class));
    }

    @Test
    void testCreateAccount_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(accountService.save(any(AccountDto.class))).thenReturn(account);

        String result = accountController.createAccount(accountDto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/accounts", result);
        verify(accountService).save(any(AccountDto.class));
        verify(redirectAttributes).addFlashAttribute("successMessage", "Account created successfully");
    }

    @Test
    void testCreateAccount_WithErrors_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        String result = accountController.createAccount(accountDto, bindingResult, model, redirectAttributes);

        assertEquals("account/form", result);
        verify(accountService, never()).save(any());
    }

    @Test
    void testShowEditForm_ShouldReturnForm() {
        when(accountService.findById(1)).thenReturn(Optional.of(account));
        when(roleRepository.findAll()).thenReturn(Arrays.asList(role));

        String result = accountController.showEditForm(1, model, redirectAttributes);

        assertEquals("account/form", result);
        verify(model).addAttribute(eq("accountDto"), any(AccountDto.class));
    }

    @Test
    void testUpdateAccount_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(accountService.update(eq(1), any(AccountDto.class))).thenReturn(account);

        String result = accountController.updateAccount(1, accountDto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/accounts", result);
        verify(accountService).update(eq(1), any(AccountDto.class));
    }

    @Test
    void testDeleteAccount_ShouldRedirect() {
        doNothing().when(accountService).deleteById(1);

        String result = accountController.deleteAccount(1, redirectAttributes);

        assertEquals("redirect:/accounts", result);
        verify(accountService).deleteById(1);
    }

    @Test
    void testShowChangePasswordForm_WithAdminAccount_ShouldRedirect() {
        when(accountService.findById(1)).thenReturn(Optional.of(account));

        String result = accountController.showChangePasswordForm(1, model, redirectAttributes);

        assertEquals("redirect:/accounts/1", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Cannot change password for admin accounts. Admin accounts can only be viewed.");
    }

    @Test
    void testShowChangePasswordForm_WithNonAdminAccount_ShouldReturnForm() {
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        account.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));
        when(accountService.findById(1)).thenReturn(Optional.of(account));

        String result = accountController.showChangePasswordForm(1, model, redirectAttributes);

        assertEquals("account/change-password", result);
        verify(model).addAttribute("account", account);
    }

    @Test
    void testChangePassword_WithValidData_ShouldRedirect() {
        AdminChangePasswordDto dto = new AdminChangePasswordDto();
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(accountService.findById(1)).thenReturn(Optional.of(account));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(bindingResult.hasFieldErrors("newPassword")).thenReturn(false);
        when(bindingResult.hasFieldErrors("confirmPassword")).thenReturn(false);
        doNothing().when(accountService).adminChangePassword(eq(1), any(AdminChangePasswordDto.class));

        String result = accountController.changePassword(1, dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/accounts/1", result);
        verify(accountService).adminChangePassword(eq(1), any(AdminChangePasswordDto.class));
    }
}

