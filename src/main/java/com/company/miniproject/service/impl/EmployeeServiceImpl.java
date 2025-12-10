package com.company.miniproject.service.impl;

import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.dto.EmployeeRegistrationDto;
import com.company.miniproject.entity.*;
import com.company.miniproject.repository.*;
import com.company.miniproject.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String keyword, Integer departmentId, Pageable pageable) {
        return employeeRepository.searchEmployees(keyword, departmentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByAccountId(Integer accountId) {
        return employeeRepository.findByAccountId(accountId);
    }

    @Override
    public Employee save(EmployeeRegistrationDto dto) {
        // Validate password for create
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Validate unique constraints with detailed error messages
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }
        // Check email uniqueness (case-insensitive)
        String normalizedEmail = dto.getEmail().trim();
        Optional<Account> existingAccount = accountRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existingAccount.isPresent()) {
            throw new IllegalArgumentException("Email already exists. This email is already used by another user. Please use a different email address.");
        }
        if (employeeRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists. Please use a different phone number.");
        }
        
        // Get department
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + dto.getDepartmentId()));
        
        // Create account with default EMPLOYEE role
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setStatus(AccountStatus.Active);
        
        // Set default role as EMPLOYEE
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role not found in database"));
        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);
        account.setRoles(roles);
        
        account = accountRepository.save(account);
        
        // Create employee
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setBirthDate(dto.getBirthDate());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setDepartment(department);
        employee.setAccount(account);
        
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Integer id, EmployeeRegistrationDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        
        Account account = employee.getAccount();
        
        // Check username uniqueness (if changed) with detailed error messages
        if (!account.getUsername().equals(dto.getUsername()) && 
            accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }
        
        // Check email uniqueness (if changed) with detailed error messages (case-insensitive)
        String normalizedNewEmail = dto.getEmail().trim();
        String normalizedCurrentEmail = account.getEmail().trim();
        
        if (!normalizedCurrentEmail.equalsIgnoreCase(normalizedNewEmail)) {
            // Check if new email already exists for another account (case-insensitive)
            Optional<Account> existingAccount = accountRepository.findByEmailIgnoreCase(normalizedNewEmail);
            if (existingAccount.isPresent() && !existingAccount.get().getId().equals(account.getId())) {
                throw new IllegalArgumentException("Email already exists. This email is already used by another user. Please use a different email address.");
            }
        }
        
        // Check phone uniqueness (if changed) with detailed error messages
        if (!employee.getPhone().equals(dto.getPhone()) && 
            employeeRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists. Please use a different phone number.");
        }
        
        // Get department
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + dto.getDepartmentId()));
        
        // Update account
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        accountRepository.save(account);
        
        // Update employee
        employee.setFullName(dto.getFullName());
        employee.setBirthDate(dto.getBirthDate());
        employee.setGender(dto.getGender());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        
        // Update department - handle bidirectional relationship properly
        Department oldDepartment = employee.getDepartment();
        if (oldDepartment != null && !oldDepartment.getId().equals(department.getId())) {
            // Remove employee from old department's employees list
            if (oldDepartment.getEmployees() != null) {
                oldDepartment.getEmployees().remove(employee);
            }
        }
        // Set new department - this will also add employee to department's employees list
        employee.setDepartment(department);
        
        return employeeRepository.save(employee);
    }

    @Override
    public void changePassword(Integer employeeId, ChangePasswordDto dto) {
        // Find employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        
        Account account = employee.getAccount();
        if (account == null) {
            throw new IllegalStateException("Employee account not found");
        }
        
        // Validate current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password matches confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Validate new password is different from current password
        if (passwordEncoder.matches(dto.getNewPassword(), account.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        // Update password
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void deleteById(Integer id) {
        // Check if employee exists
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        
        // Delete employee (account will be cascade deleted)
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return employeeRepository.existsByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
}
