package com.company.miniproject.service.impl;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.*;
import com.company.miniproject.repository.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private Account account;
    private Department department;
    private Role role;
    private EmployeeRegistrationDto dto;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1);
        department.setName("IT");

        role = new Role();
        role.setId(1);
        role.setName("EMPLOYEE");

        account = new Account();
        account.setId(1);
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setPassword("$2a$10$encoded");
        account.setStatus(AccountStatus.Active);

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");
        employee.setPhone("0912345678");
        employee.setDepartment(department);
        employee.setAccount(account);

        dto = new EmployeeRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");
        dto.setFullName("Jane Smith");
        dto.setPhone("0923456789");
        dto.setDepartmentId(1);
    }

    @Test
    void testFindAll_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);

        Page<Employee> result = employeeService.findAll(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindById_WhenExists_ShouldReturnEmployee() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getFullName());
    }

    @Test
    void testSave_WithValidData_ShouldSave() {
        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        when(accountRepository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.existsByPhone("0923456789")).thenReturn(false);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.save(dto);

        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testSave_WithEmptyPassword_ShouldThrowException() {
        dto.setPassword("");

        assertThrows(IllegalArgumentException.class, () -> {
            employeeService.save(dto);
        });
    }

    @Test
    void testUpdate_WithValidData_ShouldUpdate() {
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        when(accountRepository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.existsByPhone("0923456789")).thenReturn(false);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.update(1, dto);

        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testDeleteById_ShouldDelete() {
        when(employeeRepository.existsById(1)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1);

        assertDoesNotThrow(() -> employeeService.deleteById(1));
        verify(employeeRepository).deleteById(1);
    }

    @Test
    void testChangePassword_WithValidData_ShouldUpdate() {
        ChangePasswordDto changeDto = new ChangePasswordDto();
        changeDto.setCurrentPassword("oldpass");
        changeDto.setNewPassword("newpass");
        changeDto.setConfirmPassword("newpass");

        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("oldpass", account.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newpass", account.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newencoded");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        assertDoesNotThrow(() -> employeeService.changePassword(1, changeDto));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testSearchEmployees_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(Arrays.asList(employee));
        when(employeeRepository.searchEmployees("John", null, pageable)).thenReturn(page);

        Page<Employee> result = employeeService.searchEmployees("John", null, pageable);

        assertEquals(1, result.getContent().size());
    }
}

