package com.company.miniproject.service.impl;

import com.company.miniproject.dto.ProjectAssignmentDto;
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

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private Employee employee;
    private ProjectAssignment assignment;
    private ProjectAssignmentDto assignmentDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1);
        project.setName("Test Project");
        project.setStartDate(LocalDate.of(2024, 1, 1));
        project.setEndDate(LocalDate.of(2024, 12, 31));
        project.setStatus(ProjectStatus.Ongoing);

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");

        assignment = new ProjectAssignment();
        assignment.setId(1);
        assignment.setProject(project);
        assignment.setEmployee(employee);
        assignment.setRoleInProject("DEVELOPER");
        assignment.setJoinDate(LocalDate.of(2024, 1, 15));

        assignmentDto = new ProjectAssignmentDto();
        assignmentDto.setProjectId(1);
        assignmentDto.setEmployeeId(1);
        assignmentDto.setRoleInProject("Developer");
        assignmentDto.setJoinDate(LocalDate.of(2024, 1, 15));
    }

    @Test
    void testFindAll_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(Arrays.asList(project));
        when(projectRepository.findAll(pageable)).thenReturn(page);

        Page<Project> result = projectService.findAll(pageable);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindById_WhenExists_ShouldReturnProject() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));

        Optional<Project> result = projectService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("Test Project", result.get().getName());
    }

    @Test
    void testSave_WithValidData_ShouldSave() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.save(project);

        assertNotNull(result);
        verify(projectRepository).save(project);
    }

    @Test
    void testSave_WithInvalidDates_ShouldThrowException() {
        project.setStartDate(LocalDate.of(2024, 12, 31));
        project.setEndDate(LocalDate.of(2024, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> {
            projectService.save(project);
        });
    }

    @Test
    void testUpdate_WithValidData_ShouldUpdate() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.update(1, project);

        assertNotNull(result);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void testDeleteById_ShouldDelete() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).deleteById(1);

        assertDoesNotThrow(() -> projectService.deleteById(1));
        verify(projectRepository).deleteById(1);
    }

    @Test
    void testGetProjectAssignments_ShouldReturnList() {
        List<ProjectAssignment> assignments = Arrays.asList(assignment);
        when(projectAssignmentRepository.findByProjectId(1)).thenReturn(assignments);

        List<ProjectAssignment> result = projectService.getProjectAssignments(1);

        assertEquals(1, result.size());
    }

    @Test
    void testAddProjectAssignment_WithValidData_ShouldSave() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.existsByProjectIdAndEmployeeId(1, 1)).thenReturn(false);
        when(projectAssignmentRepository.save(any(ProjectAssignment.class))).thenReturn(assignment);

        ProjectAssignment result = projectService.addProjectAssignment(assignmentDto);

        assertNotNull(result);
        verify(projectAssignmentRepository).save(any(ProjectAssignment.class));
    }

    @Test
    void testAddProjectAssignment_WithDuplicate_ShouldThrowException() {
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.existsByProjectIdAndEmployeeId(1, 1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            projectService.addProjectAssignment(assignmentDto);
        });
    }

    @Test
    void testAddProjectAssignment_WithInvalidJoinDate_ShouldThrowException() {
        assignmentDto.setJoinDate(LocalDate.of(2023, 12, 1));
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(projectAssignmentRepository.existsByProjectIdAndEmployeeId(1, 1)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            projectService.addProjectAssignment(assignmentDto);
        });
    }

    @Test
    void testRemoveProjectAssignment_ShouldDelete() {
        when(projectAssignmentRepository.findById(1)).thenReturn(Optional.of(assignment));
        doNothing().when(projectAssignmentRepository).deleteById(1);

        assertDoesNotThrow(() -> projectService.removeProjectAssignment(1));
        verify(projectAssignmentRepository).deleteById(1);
    }
}

