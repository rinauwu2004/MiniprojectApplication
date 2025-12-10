package com.company.miniproject.service;

import com.company.miniproject.dto.ProjectAssignmentDto;
import com.company.miniproject.entity.Project;
import com.company.miniproject.entity.ProjectAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    
    Page<Project> findAll(Pageable pageable);
    
    Page<Project> findByEmployeeId(Integer employeeId, Pageable pageable);
    
    Optional<Project> findById(Integer id);
    
    Project save(Project project);
    
    Project update(Integer id, Project project);
    
    void deleteById(Integer id);
    
    List<ProjectAssignment> getProjectAssignments(Integer projectId);
    
    ProjectAssignment addProjectAssignment(ProjectAssignmentDto dto);
    
    void removeProjectAssignment(Integer assignmentId);
    
    boolean existsByName(String name);
}
