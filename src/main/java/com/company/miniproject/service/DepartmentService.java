package com.company.miniproject.service;

import com.company.miniproject.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    
    List<Department> findAll();
    
    List<Department> findAll(String sortBy, boolean ascending);
    
    Optional<Department> findById(Integer id);
    
    Department save(Department department);
    
    Department update(Integer id, Department department);
    
    void deleteById(Integer id) throws IllegalStateException;
    
    boolean existsByName(String name);
    
    boolean hasEmployees(Integer departmentId);
    
    void addEmployeeToDepartment(Integer departmentId, Integer employeeId);
    
    void removeEmployeeFromDepartment(Integer departmentId, Integer employeeId);
}


