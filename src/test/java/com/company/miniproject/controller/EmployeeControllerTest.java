package com.company.miniproject.controller;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.service.DepartmentService;
import com.company.miniproject.service.EmployeeService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;
    private EmployeeRegistrationDto dto;
    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1);
        department.setName("IT");

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");
        employee.setPhone("0912345678");

        dto = new EmployeeRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");
        dto.setFullName("Jane Smith");
        dto.setPhone("0923456789");
        dto.setDepartmentId(1);
    }

    @Test
    void testListEmployees_ShouldReturnList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeService.findAll(pageable)).thenReturn(page);
        when(departmentService.findAll()).thenReturn(Arrays.asList(department));

        String result = employeeController.listEmployees(null, null, 0, 10, model);

        assertEquals("employee/list", result);
        verify(model).addAttribute("employees", page.getContent());
    }

    @Test
    void testListEmployees_WithFilters_ShouldUseSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeService.searchEmployees("John", 1, pageable)).thenReturn(page);
        when(departmentService.findAll()).thenReturn(Arrays.asList(department));

        String result = employeeController.listEmployees("John", 1, 0, 10, model);

        assertEquals("employee/list", result);
        verify(employeeService).searchEmployees("John", 1, pageable);
    }

    @Test
    void testShowCreateForm_ShouldReturnForm() {
        when(departmentService.findAll()).thenReturn(Arrays.asList(department));

        String result = employeeController.showCreateForm(model);

        assertEquals("employee/form", result);
        verify(model).addAttribute(eq("employeeDto"), any(EmployeeRegistrationDto.class));
    }

    @Test
    void testCreateEmployee_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(employeeService.save(any(EmployeeRegistrationDto.class))).thenReturn(employee);

        String result = employeeController.createEmployee(dto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/employees", result);
        verify(employeeService).save(any(EmployeeRegistrationDto.class));
    }

    @Test
    void testCreateEmployee_WithEmptyPassword_ShouldReturnForm() {
        dto.setPassword("");
        when(bindingResult.hasErrors()).thenReturn(true);
        when(departmentService.findAll()).thenReturn(Arrays.asList(department));

        String result = employeeController.createEmployee(dto, bindingResult, model, redirectAttributes);

        assertEquals("employee/form", result);
        verify(employeeService, never()).save(any());
    }

    @Test
    void testShowEditForm_ShouldReturnForm() {
        com.company.miniproject.entity.Account account = new com.company.miniproject.entity.Account();
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        employee.setAccount(account);
        when(employeeService.findById(1)).thenReturn(Optional.of(employee));
        when(departmentService.findAll()).thenReturn(Arrays.asList(department));

        String result = employeeController.showEditForm(1, model);

        assertEquals("employee/form", result);
        verify(model).addAttribute(eq("employeeDto"), any(EmployeeRegistrationDto.class));
    }

    @Test
    void testUpdateEmployee_WithValidData_ShouldRedirect() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(employeeService.update(eq(1), any(EmployeeRegistrationDto.class))).thenReturn(employee);

        String result = employeeController.updateEmployee(1, dto, bindingResult, model, authentication, redirectAttributes);

        assertEquals("redirect:/employees", result);
        verify(employeeService).update(eq(1), any(EmployeeRegistrationDto.class));
    }

    @Test
    void testDeleteEmployee_ShouldRedirect() {
        doNothing().when(employeeService).deleteById(1);

        String result = employeeController.deleteEmployee(1, redirectAttributes);

        assertEquals("redirect:/employees", result);
        verify(employeeService).deleteById(1);
    }

    @Test
    void testShowChangePasswordForm_ShouldReturnForm() {
        when(employeeService.findById(1)).thenReturn(Optional.of(employee));

        String result = employeeController.showChangePasswordForm(1, model);

        assertEquals("employee/change-password", result);
        verify(model).addAttribute("employee", employee);
    }

    @Test
    void testChangePassword_WithValidData_ShouldRedirect() {
        ChangePasswordDto changeDto = new ChangePasswordDto();
        changeDto.setCurrentPassword("oldpass");
        changeDto.setNewPassword("newpass");
        changeDto.setConfirmPassword("newpass");

        when(employeeService.findById(1)).thenReturn(Optional.of(employee));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(bindingResult.hasFieldErrors("newPassword")).thenReturn(false);
        when(bindingResult.hasFieldErrors("confirmPassword")).thenReturn(false);
        doNothing().when(employeeService).changePassword(eq(1), any(ChangePasswordDto.class));

        String result = employeeController.changePassword(1, changeDto, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/logout", result);
        verify(employeeService).changePassword(eq(1), any(ChangePasswordDto.class));
    }
}

