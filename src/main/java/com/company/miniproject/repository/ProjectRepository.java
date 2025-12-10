package com.company.miniproject.repository;

import com.company.miniproject.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.assignments pa WHERE pa.employee.id = :employeeId")
    Page<Project> findByEmployeeId(@Param("employeeId") Integer employeeId, Pageable pageable);
}
