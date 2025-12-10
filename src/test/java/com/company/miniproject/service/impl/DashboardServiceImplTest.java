package com.company.miniproject.service.impl;

import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.entity.ProjectAssignment;
import com.company.miniproject.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetStatistics_AsAdmin_ShouldReturnAllStats() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(departmentRepository.count()).thenReturn(3L);
        when(accountRepository.count()).thenReturn(10L);
        when(projectRepository.count()).thenReturn(5L);

        Map<String, Long> stats = dashboardService.getStatistics(authentication);

        assertEquals(3, stats.size());
        assertEquals(3L, stats.get("totalDepartments"));
        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(5L, stats.get("totalProjects"));
    }

    @Test
    void testGetStatistics_AsManager_ShouldReturnManagerStats() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_MANAGER")
        );

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(departmentRepository.count()).thenReturn(3L);
        when(employeeRepository.countEmployeesByRoleAndStatus(AccountStatus.Active)).thenReturn(5L);
        when(projectRepository.count()).thenReturn(5L);

        Map<String, Long> stats = dashboardService.getStatistics(authentication);

        assertEquals(3, stats.size());
        assertEquals(3L, stats.get("totalDepartments"));
        assertEquals(5L, stats.get("totalEmployees"));
        assertEquals(5L, stats.get("totalProjects"));
    }

    @Test
    void testGetStatistics_AsEmployee_ShouldReturnEmployeeStats() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        );

        Account account = new Account();
        account.setId(1);
        account.setUsername("testuser");

        Employee employee = new Employee();
        employee.setId(1);

        List<ProjectAssignment> assignments = Arrays.asList(new ProjectAssignment(), new ProjectAssignment());

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.findByEmployeeId(1)).thenReturn(assignments);

        Map<String, Long> stats = dashboardService.getStatistics(authentication);

        assertEquals(1, stats.size());
        assertEquals(2L, stats.get("myProjects"));
    }

    @Test
    void testGetStatistics_AsEmployee_WithNoEmployeeRecord_ShouldReturnZero() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        );

        Account account = new Account();
        account.setId(1);
        account.setUsername("testuser");

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.empty());

        Map<String, Long> stats = dashboardService.getStatistics(authentication);

        assertEquals(1, stats.size());
        assertEquals(0L, stats.get("myProjects"));
    }

    @Test
    void testGetStatistics_AsEmployee_WithNoAccount_ShouldReturnZero() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        );

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Map<String, Long> stats = dashboardService.getStatistics(authentication);

        assertEquals(1, stats.size());
        assertEquals(0L, stats.get("myProjects"));
    }

    @Test
    void testGetStatistics_WithNullAuthentication_ShouldReturnEmpty() {
        Map<String, Long> stats = dashboardService.getStatistics(null);

        assertTrue(stats.isEmpty());
    }
}

