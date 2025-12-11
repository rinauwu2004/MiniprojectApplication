package com.company.miniproject.repository;

import com.company.miniproject.entity.Employee;
import com.company.miniproject.entity.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    
    @Query("SELECT COUNT(e) FROM Employee e JOIN e.account a WHERE a.status = :status")
    long countByAccountStatus(@Param("status") com.company.miniproject.entity.AccountStatus status);
    
    @Query("SELECT d.name, COUNT(e) FROM Employee e JOIN e.department d GROUP BY d.id, d.name")
    List<Object[]> countEmployeesByDepartment();
    
    @Query("SELECT e.gender, COUNT(e) FROM Employee e GROUP BY e.gender")
    List<Object[]> countEmployeesByGender();
    
    @Query("SELECT e FROM Employee e JOIN e.department d JOIN e.account a WHERE d.id = :departmentId AND a.status = :status")
    List<Employee> findByDepartmentIdAndAccountStatus(@Param("departmentId") Integer departmentId, 
                                                       @Param("status") AccountStatus status);
    
    @Query("SELECT COUNT(e) FROM Employee e JOIN e.department d WHERE d.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Integer departmentId);
}
