package com.company.miniproject.controller;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.entity.ProjectAssignment;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.DepartmentRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.repository.ProjectAssignmentRepository;
import com.company.miniproject.service.AccountService;
import com.company.miniproject.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProfileController profileController;

    private Account account;
    private Employee employee;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1);
        account.setUsername("testuser");
        account.setEmail("test@example.com");

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");
        employee.setAccount(account);
    }

    @Test
    void testShowProfile_WhenNotAuthenticated_ShouldRedirect() {
        String result = profileController.showProfile(null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void testShowProfile_WhenAuthenticated_ShouldReturnView() {
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));

        String result = profileController.showProfile(authentication, model);

        assertEquals("profile/view", result);
        verify(model).addAttribute("account", account);
        verify(model).addAttribute("employee", employee);
    }

    @Test
    void testShowChangePasswordForm_ShouldReturnForm() {
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        String result = profileController.showChangePasswordForm(authentication, model);

        assertEquals("profile/change-password", result);
        verify(model).addAttribute("account", account);
        verify(model).addAttribute(eq("changePasswordDto"), any(ChangePasswordDto.class));
    }

    @Test
    void testChangePassword_WithValidData_ShouldRedirect() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(bindingResult.hasFieldErrors("newPassword")).thenReturn(false);
        when(bindingResult.hasFieldErrors("confirmPassword")).thenReturn(false);
        doNothing().when(accountService).changePassword(eq(1), any(ChangePasswordDto.class));

        String result = profileController.changePassword(authentication, dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/logout", result);
        verify(accountService).changePassword(eq(1), any(ChangePasswordDto.class));
    }

    @Test
    void testShowEditProfileForm_ShouldReturnForm() {
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(departmentRepository.findAll()).thenReturn(Arrays.asList());

        String result = profileController.showEditProfileForm(authentication, model);

        assertEquals("profile/edit", result);
        verify(model).addAttribute(eq("employeeDto"), any(EmployeeRegistrationDto.class));
    }

    @Test
    void testUpdateProfile_WithValidData_ShouldRedirect() {
        EmployeeRegistrationDto dto = new EmployeeRegistrationDto();
        dto.setFullName("John Updated");
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");

        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(employeeService.update(eq(1), any(EmployeeRegistrationDto.class))).thenReturn(employee);

        String result = profileController.updateProfile(authentication, dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/profile", result);
        verify(employeeService).update(eq(1), any(EmployeeRegistrationDto.class));
    }

    @Test
    void testShowEmployeeProfile_ShouldReturnView() {
        List<ProjectAssignment> assignments = Arrays.asList();
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.findByEmployeeId(1)).thenReturn(assignments);

        String result = profileController.showEmployeeProfile(authentication, model);

        assertEquals("employee/profile", result);
        verify(model).addAttribute("employee", employee);
        verify(model).addAttribute("assignments", assignments);
    }

    @Test
    void testShowMyProjects_ShouldReturnView() {
        List<ProjectAssignment> assignments = Arrays.asList();
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.findByEmployeeId(1)).thenReturn(assignments);

        String result = profileController.showMyProjects(authentication, model);

        assertEquals("employee/projects", result);
        verify(model).addAttribute("assignments", assignments);
    }
}

