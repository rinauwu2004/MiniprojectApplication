package com.company.miniproject.controller;

import com.company.miniproject.dto.ProjectAssignmentDto;
import com.company.miniproject.entity.*;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.service.ProjectService;
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

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProjectController projectController;

    private Project project;
    private ProjectAssignment assignment;
    private ProjectAssignmentDto assignmentDto;
    private Employee employee;
    private Account account;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1);
        project.setName("Test Project");
        project.setStartDate(LocalDate.of(2024, 1, 1));
        project.setEndDate(LocalDate.of(2024, 12, 31));
        project.setStatus(ProjectStatus.Ongoing);

        account = new Account();
        account.setId(1);
        account.setUsername("testuser");

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");
        employee.setAccount(account);

        assignment = new ProjectAssignment();
        assignment.setId(1);
        assignment.setProject(project);
        assignment.setEmployee(employee);

        assignmentDto = new ProjectAssignmentDto();
        assignmentDto.setProjectId(1);
        assignmentDto.setEmployeeId(1);
        assignmentDto.setRoleInProject("Developer");
        assignmentDto.setJoinDate(LocalDate.of(2024, 1, 15));
    }

    @Test
    void testListProjects_AsAdmin_ShouldReturnAllProjects() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(Arrays.asList(project));

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(projectService.findAll(pageable)).thenReturn(page);

        String result = projectController.listProjects(0, 10, model, authentication);

        assertEquals("project/list", result);
        verify(projectService).findAll(pageable);
    }

    @Test
    void testListProjects_AsEmployee_ShouldReturnAssignedProjects() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(Arrays.asList(project));

        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(authentication.getName()).thenReturn("testuser");
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(employeeRepository.findByAccountId(1)).thenReturn(Optional.of(employee));
        when(projectService.findByEmployeeId(1, pageable)).thenReturn(page);

        String result = projectController.listProjects(0, 10, model, authentication);

        assertEquals("project/list", result);
        verify(projectService).findByEmployeeId(1, pageable);
    }

    @Test
    void testShowCreateForm_ShouldReturnForm() {
        String result = projectController.showCreateForm(model);

        assertEquals("project/form", result);
        verify(model).addAttribute(eq("project"), any(Project.class));
    }

    @Test
    void testCreateProject_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(projectService.save(any(Project.class))).thenReturn(project);

        String result = projectController.createProject(project, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/projects", result);
        verify(projectService).save(any(Project.class));
    }

    @Test
    void testCreateProject_WithInvalidDates_ShouldReturnForm() {
        project.setStartDate(LocalDate.of(2024, 12, 31));
        project.setEndDate(LocalDate.of(2024, 1, 1));
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = projectController.createProject(project, bindingResult, model, redirectAttributes);

        assertEquals("project/form", result);
        verify(projectService, never()).save(any());
    }

    @Test
    void testShowProjectDetail_ShouldReturnDetail() {
        List<ProjectAssignment> assignments = Arrays.asList(assignment);
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        when(projectService.findById(1)).thenReturn(Optional.of(project));
        when(projectService.getProjectAssignments(1)).thenReturn(assignments);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(employeeRepository.findAllWithDepartment()).thenReturn(Arrays.asList(employee));

        String result = projectController.showProjectDetail(1, model, authentication);

        assertEquals("project/detail", result);
        verify(model).addAttribute("project", project);
        verify(model).addAttribute("assignments", assignments);
    }

    @Test
    void testShowEditForm_ShouldReturnForm() {
        when(projectService.findById(1)).thenReturn(Optional.of(project));

        String result = projectController.showEditForm(1, model);

        assertEquals("project/form", result);
        verify(model).addAttribute("project", project);
    }

    @Test
    void testUpdateProject_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(projectService.update(eq(1), any(Project.class))).thenReturn(project);

        String result = projectController.updateProject(1, project, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/projects/1", result);
        verify(projectService).update(eq(1), any(Project.class));
    }

    @Test
    void testDeleteProject_ShouldRedirect() {
        doNothing().when(projectService).deleteById(1);

        String result = projectController.deleteProject(1, redirectAttributes);

        assertEquals("redirect:/projects", result);
        verify(projectService).deleteById(1);
    }

    @Test
    void testAddProjectAssignment_WithValidData_ShouldRedirect() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        List<ProjectAssignment> assignments = Arrays.asList();
        List<Employee> employees = Arrays.asList(employee);

        when(projectService.findById(1)).thenReturn(Optional.of(project));
        when(projectService.getProjectAssignments(1)).thenReturn(assignments);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(employeeRepository.findAllWithDepartment()).thenReturn(employees);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(projectService.addProjectAssignment(any(ProjectAssignmentDto.class))).thenReturn(assignment);

        String result = projectController.addProjectAssignment(1, assignmentDto, bindingResult, model, authentication, redirectAttributes);

        assertEquals("redirect:/projects/1", result);
        verify(projectService).addProjectAssignment(any(ProjectAssignmentDto.class));
    }

    @Test
    void testRemoveProjectAssignment_ShouldRedirect() {
        doNothing().when(projectService).removeProjectAssignment(1);

        String result = projectController.removeProjectAssignment(1, 1, redirectAttributes);

        assertEquals("redirect:/projects/1", result);
        verify(projectService).removeProjectAssignment(1);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Assignment removed successfully");
    }
}

