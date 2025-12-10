package com.company.miniproject.repository;

import com.company.miniproject.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.account WHERE e.account.id = :accountId")
    Optional<Employee> findByAccountId(@Param("accountId") Integer accountId);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId)")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, 
                                   @Param("departmentId") Integer departmentId, 
                                   Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployeesByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId")
    Page<Employee> findByDepartmentId(@Param("departmentId") Integer departmentId, Pageable pageable);
    
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.department")
    java.util.List<Employee> findAllWithDepartment();
    
    @Query("SELECT COUNT(e) FROM Employee e JOIN e.account a JOIN a.roles r WHERE r.name = 'EMPLOYEE' AND a.status = :status")
    long countEmployeesByRoleAndStatus(@Param("status") com.company.miniproject.entity.AccountStatus status);
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.account WHERE e.department IS NULL OR e.department.id != :departmentId")
    java.util.List<Employee> findEmployeesNotInDepartment(@Param("departmentId") Integer departmentId);
    
    boolean existsByPhone(String phone);
}
