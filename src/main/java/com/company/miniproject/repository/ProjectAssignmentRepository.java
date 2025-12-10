package com.company.miniproject.repository;

import com.company.miniproject.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Integer> {
    
    @Query("SELECT pa FROM ProjectAssignment pa LEFT JOIN FETCH pa.project LEFT JOIN FETCH pa.employee WHERE pa.project.id = :projectId")
    List<ProjectAssignment> findByProjectId(@Param("projectId") Integer projectId);
    
    @Query("SELECT pa FROM ProjectAssignment pa LEFT JOIN FETCH pa.project LEFT JOIN FETCH pa.employee WHERE pa.employee.id = :employeeId")
    List<ProjectAssignment> findByEmployeeId(@Param("employeeId") Integer employeeId);
    
    @Query("SELECT pa FROM ProjectAssignment pa WHERE pa.project.id = :projectId AND pa.employee.id = :employeeId")
    Optional<ProjectAssignment> findByProjectIdAndEmployeeId(@Param("projectId") Integer projectId, 
                                                              @Param("employeeId") Integer employeeId);
    
    boolean existsByProjectIdAndEmployeeId(Integer projectId, Integer employeeId);
}

