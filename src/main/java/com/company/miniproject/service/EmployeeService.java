package com.company.miniproject.service;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeService {
    
    Page<Employee> findAll(Pageable pageable);
    
    Page<Employee> searchEmployees(String keyword, Integer departmentId, Pageable pageable);
    
    Optional<Employee> findById(Integer id);
    
    Optional<Employee> findByAccountId(Integer accountId);
    
    Employee save(EmployeeRegistrationDto dto);
    
    Employee update(Integer id, EmployeeRegistrationDto dto);
    
    void changePassword(Integer employeeId, ChangePasswordDto dto);
    
    void deleteById(Integer id);
    
    boolean existsByPhone(String phone);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
