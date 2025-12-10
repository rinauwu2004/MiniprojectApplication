package com.company.miniproject.service.impl;

import com.company.miniproject.repository.*;
import com.company.miniproject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Override
    public Map<String, Long> getStatistics(Authentication authentication) {
        Map<String, Long> stats = new HashMap<>();
        
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        
        if (isAdmin) {
            stats.put("totalDepartments", departmentRepository.count());
            stats.put("totalUsers", accountRepository.count()); // Total users instead of employees
            stats.put("totalProjects", projectRepository.count());
        }
        
        if (isManager && !isAdmin) {
            stats.put("totalDepartments", departmentRepository.count());
            long activeEmployeeCount = employeeRepository.countEmployeesByRoleAndStatus(
                com.company.miniproject.entity.AccountStatus.Active);
            stats.put("totalEmployees", activeEmployeeCount);
            stats.put("totalProjects", projectRepository.count());
        }
        
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"))) {
            String username = authentication.getName();
            Optional<com.company.miniproject.entity.Account> accountOpt = 
                accountRepository.findByUsername(username);
            
            if (accountOpt.isPresent()) {
                Optional<com.company.miniproject.entity.Employee> employeeOpt = 
                    employeeRepository.findByAccountId(accountOpt.get().getId());
                
                if (employeeOpt.isPresent()) {
                    long myProjectsCount = projectAssignmentRepository
                        .findByEmployeeId(employeeOpt.get().getId()).size();
                    stats.put("myProjects", myProjectsCount);
                } else {
                    stats.put("myProjects", 0L);
                }
            } else {
                stats.put("myProjects", 0L);
            }
        }
        
        return stats;
    }
}

