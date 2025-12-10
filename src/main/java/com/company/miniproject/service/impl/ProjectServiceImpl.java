package com.company.miniproject.service.impl;

import com.company.miniproject.dto.ProjectAssignmentDto;
import com.company.miniproject.entity.*;
import com.company.miniproject.repository.*;
import com.company.miniproject.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Project> findByEmployeeId(Integer employeeId, Pageable pageable) {
        return projectRepository.findByEmployeeId(employeeId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Project> findById(Integer id) {
        return projectRepository.findById(id);
    }

    @Override
    public Project save(Project project) {
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEndDate().isBefore(project.getStartDate()) || 
                project.getEndDate().isEqual(project.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }
        
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.Planning);
        }
        
        return projectRepository.save(project);
    }

    @Override
    public Project update(Integer id, Project project) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        
        if (project.getStartDate() != null && project.getEndDate() != null) {
            if (project.getEndDate().isBefore(project.getStartDate()) || 
                project.getEndDate().isEqual(project.getStartDate())) {
                throw new IllegalArgumentException("End date must be after start date");
            }
        }
        
        existingProject.setName(project.getName());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        existingProject.setStatus(project.getStatus());
        
        return projectRepository.save(existingProject);
    }

    @Override
    public void deleteById(Integer id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
        
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignment> getProjectAssignments(Integer projectId) {
        return projectAssignmentRepository.findByProjectId(projectId);
    }

    @Override
    public ProjectAssignment addProjectAssignment(ProjectAssignmentDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + dto.getProjectId()));
        
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + dto.getEmployeeId()));
        
        if (projectAssignmentRepository.existsByProjectIdAndEmployeeId(
                dto.getProjectId(), dto.getEmployeeId())) {
            throw new IllegalArgumentException("This employee is already assigned to this project");
        }
        
        LocalDate joinDate = dto.getJoinDate() != null ? dto.getJoinDate() : LocalDate.now();
        if (project.getStartDate() != null && joinDate.isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("Cannot add member before project start date. Please select a join date on or after the project start date.");
        }
        if (project.getEndDate() != null && joinDate.isAfter(project.getEndDate())) {
            throw new IllegalArgumentException("Cannot add member after project end date. Please select a join date on or before the project end date.");
        }
        
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setEmployee(employee);
        assignment.setRoleInProject(dto.getRoleInProject() != null ? dto.getRoleInProject().trim().toUpperCase() : null);
        assignment.setJoinDate(joinDate);
        
        return projectAssignmentRepository.save(assignment);
    }

    @Override
    public void removeProjectAssignment(Integer assignmentId) {
        projectAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Project assignment not found with id: " + assignmentId));
        
        projectAssignmentRepository.deleteById(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return projectRepository.existsByName(name);
    }
}
