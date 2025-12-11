package com.company.miniproject.service.impl;

import com.company.miniproject.entity.*;
import com.company.miniproject.repository.*;
import com.company.miniproject.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
            stats.put("totalUsers", accountRepository.count());
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
    
    @Override
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        long activeCount = accountRepository.countEmployeesAndManagersByStatus(AccountStatus.Active);
        data.put("activeEmployeesAndManagers", activeCount);
        
        long blockedCount = accountRepository.countEmployeesAndManagersByStatus(AccountStatus.Blocked);
        data.put("blockedEmployeesAndManagers", blockedCount);
        
        List<Object[]> deptCounts = employeeRepository.countEmployeesByDepartment();
        Map<String, Long> employeesByDept = new LinkedHashMap<>();
        for (Object[] row : deptCounts) {
            String deptName = (String) row[0];
            Long count = (Long) row[1];
            employeesByDept.put(deptName, count);
        }
        data.put("employeesByDepartment", employeesByDept);
        
        List<Object[]> genderCounts = employeeRepository.countEmployeesByGender();
        Map<String, Long> genderDistribution = new LinkedHashMap<>();
        for (Object[] row : genderCounts) {
            Gender gender = (Gender) row[0];
            Long count = (Long) row[1];
            genderDistribution.put(gender.name(), count);
        }
        data.put("genderDistribution", genderDistribution);
        
        return data;
    }
    
    @Override
    public Map<String, Object> getManagerDashboardData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        
        if (authentication == null) {
            return data;
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        
        if (accountOpt.isPresent()) {
            Optional<Employee> managerOpt = employeeRepository.findByAccountId(accountOpt.get().getId());
            
            if (managerOpt.isPresent()) {
                Employee manager = managerOpt.get();
                Department managerDept = manager.getDepartment();
                
                if (managerDept != null) {
                    Integer deptId = managerDept.getId();
                    
                    long deptEmployeeCount = employeeRepository.countByDepartmentId(deptId);
                    data.put("departmentEmployeeCount", deptEmployeeCount);
                    
                    long totalEmployees = employeeRepository.countEmployeesByRoleAndStatus(AccountStatus.Active) +
                                        employeeRepository.countEmployeesByRoleAndStatus(AccountStatus.Blocked);
                    data.put("totalEmployees", totalEmployees);
                    
                    List<Employee> activeEmployees = employeeRepository.findByDepartmentIdAndAccountStatus(
                        deptId, AccountStatus.Active);
                    data.put("activeEmployeesInDept", (long) activeEmployees.size());
                    
                    List<Employee> blockedEmployees = employeeRepository.findByDepartmentIdAndAccountStatus(
                        deptId, AccountStatus.Blocked);
                    data.put("blockedEmployeesInDept", (long) blockedEmployees.size());
                    
                    data.put("departmentName", managerDept.getName());
                }
            }
        }
        
        return data;
    }
    
    @Override
    public Map<String, Object> getEmployeeDashboardData(Authentication authentication) {
        Map<String, Object> data = new HashMap<>();
        
        if (authentication == null) {
            return data;
        }
        
        String username = authentication.getName();
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        
        if (accountOpt.isPresent()) {
            Optional<Employee> employeeOpt = employeeRepository.findByAccountId(accountOpt.get().getId());
            
            if (employeeOpt.isPresent()) {
                Employee employee = employeeOpt.get();
                
                List<ProjectAssignment> assignments = projectAssignmentRepository.findByEmployeeId(employee.getId());
                data.put("projectCount", (long) assignments.size());
                
                Map<String, Long> projectDurations = new LinkedHashMap<>();
                LocalDate now = LocalDate.now();
                
                for (ProjectAssignment assignment : assignments) {
                    Project project = assignment.getProject();
                    String projectName = project.getName();
                    
                    LocalDate startDate = assignment.getJoinDate() != null ? 
                        assignment.getJoinDate() : project.getStartDate();
                    LocalDate endDate = project.getEndDate() != null ? 
                        project.getEndDate() : now;
                    
                    if (startDate != null) {
                        long days = ChronoUnit.DAYS.between(startDate, endDate);
                        projectDurations.put(projectName, days);
                    }
                }
                data.put("projectDurations", projectDurations);
                
                data.put("employeeName", employee.getFullName());
                data.put("employeeEmail", employee.getAccount().getEmail());
                data.put("employeePhone", employee.getPhone());
                data.put("employeeDepartment", employee.getDepartment() != null ? 
                    employee.getDepartment().getName() : "N/A");
            }
        }
        
        return data;
    }
}

