package com.company.miniproject.config;

import com.company.miniproject.entity.*;
import com.company.miniproject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only initialize if roles don't exist
        if (roleRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        // 1. Create Roles
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole = roleRepository.save(adminRole);

        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        managerRole = roleRepository.save(managerRole);

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        employeeRole = roleRepository.save(employeeRole);

        // 2. Create Departments
        Department itDept = new Department();
        itDept.setName("IT");
        itDept.setDescription("Information Technology Department - Handles all IT infrastructure and software development");
        itDept = departmentRepository.save(itDept);

        Department hrDept = new Department();
        hrDept.setName("HR");
        hrDept.setDescription("Human Resources Department - Manages employee relations, recruitment, and benefits");
        hrDept = departmentRepository.save(hrDept);

        Department salesDept = new Department();
        salesDept.setName("Sales");
        salesDept.setDescription("Sales Department - Responsible for business development and customer relations");
        salesDept = departmentRepository.save(salesDept);

        // 3. Create Admin Accounts
        Account admin1 = createAccount("admin1", "123456", "admin1@company.com", AccountStatus.Active);
        Set<Role> admin1Roles = new HashSet<>();
        admin1Roles.add(adminRole);
        admin1.setRoles(admin1Roles);
        admin1 = accountRepository.save(admin1);

        Account admin2 = createAccount("admin2", "123456", "admin2@company.com", AccountStatus.Active);
        Set<Role> admin2Roles = new HashSet<>();
        admin2Roles.add(adminRole);
        admin2.setRoles(admin2Roles);
        admin2 = accountRepository.save(admin2);

        // 4. Create Manager Account
        Account manager1 = createAccount("manager1", "123456", "manager1@company.com", AccountStatus.Active);
        Set<Role> manager1Roles = new HashSet<>();
        manager1Roles.add(managerRole);
        manager1Roles.add(employeeRole); // Manager can also have EMPLOYEE role
        manager1.setRoles(manager1Roles);
        manager1 = accountRepository.save(manager1);

        // 5. Create Employee Accounts and Employees
        Account emp1Account = createAccount("emp1", "123456", "emp1@company.com", AccountStatus.Active);
        Set<Role> emp1Roles = new HashSet<>();
        emp1Roles.add(employeeRole);
        emp1Account.setRoles(emp1Roles);
        emp1Account = accountRepository.save(emp1Account);
        
        Employee emp1 = new Employee();
        emp1.setFullName("John Doe");
        emp1.setBirthDate(LocalDate.of(1990, 5, 15));
        emp1.setGender(Gender.Male);
        emp1.setPhone("0912345678");
        emp1.setAddress("123 Main Street, Ho Chi Minh City");
        emp1.setDepartment(itDept);
        emp1.setAccount(emp1Account);
        emp1 = employeeRepository.save(emp1);

        Account emp2Account = createAccount("emp2", "123456", "emp2@company.com", AccountStatus.Active);
        Set<Role> emp2Roles = new HashSet<>();
        emp2Roles.add(employeeRole);
        emp2Account.setRoles(emp2Roles);
        emp2Account = accountRepository.save(emp2Account);
        
        Employee emp2 = new Employee();
        emp2.setFullName("Jane Smith");
        emp2.setBirthDate(LocalDate.of(1992, 8, 20));
        emp2.setGender(Gender.Female);
        emp2.setPhone("0923456789");
        emp2.setAddress("456 Oak Avenue, Hanoi");
        emp2.setDepartment(hrDept);
        emp2.setAccount(emp2Account);
        emp2 = employeeRepository.save(emp2);

        Account emp3Account = createAccount("emp3", "123456", "emp3@company.com", AccountStatus.Active);
        Set<Role> emp3Roles = new HashSet<>();
        emp3Roles.add(employeeRole);
        emp3Account.setRoles(emp3Roles);
        emp3Account = accountRepository.save(emp3Account);
        
        Employee emp3 = new Employee();
        emp3.setFullName("Bob Johnson");
        emp3.setBirthDate(LocalDate.of(1988, 3, 10));
        emp3.setGender(Gender.Male);
        emp3.setPhone("0934567890");
        emp3.setAddress("789 Pine Road, Da Nang");
        emp3.setDepartment(itDept);
        emp3.setAccount(emp3Account);
        emp3 = employeeRepository.save(emp3);

        Account emp4Account = createAccount("emp4", "123456", "emp4@company.com", AccountStatus.Active);
        Set<Role> emp4Roles = new HashSet<>();
        emp4Roles.add(employeeRole);
        emp4Account.setRoles(emp4Roles);
        emp4Account = accountRepository.save(emp4Account);
        
        Employee emp4 = new Employee();
        emp4.setFullName("Alice Williams");
        emp4.setBirthDate(LocalDate.of(1995, 11, 25));
        emp4.setGender(Gender.Female);
        emp4.setPhone("0945678901");
        emp4.setAddress("321 Elm Street, Can Tho");
        emp4.setDepartment(salesDept);
        emp4.setAccount(emp4Account);
        emp4 = employeeRepository.save(emp4);

        Account emp5Account = createAccount("emp5", "123456", "emp5@company.com", AccountStatus.Active);
        Set<Role> emp5Roles = new HashSet<>();
        emp5Roles.add(employeeRole);
        emp5Account.setRoles(emp5Roles);
        emp5Account = accountRepository.save(emp5Account);
        
        Employee emp5 = new Employee();
        emp5.setFullName("Charlie Brown");
        emp5.setBirthDate(LocalDate.of(1991, 7, 30));
        emp5.setGender(Gender.Male);
        emp5.setPhone("0956789012");
        emp5.setAddress("654 Maple Drive, Hai Phong");
        emp5.setDepartment(itDept);
        emp5.setAccount(emp5Account);
        emp5 = employeeRepository.save(emp5);

        // 6. Create Projects
        Project project1 = new Project();
        project1.setName("E-Commerce Platform Development");
        project1.setStartDate(LocalDate.of(2024, 1, 1));
        project1.setEndDate(LocalDate.of(2024, 12, 31));
        project1.setStatus(ProjectStatus.Ongoing);
        project1 = projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Customer Management System");
        project2.setStartDate(LocalDate.of(2024, 3, 1));
        project2.setEndDate(LocalDate.of(2024, 9, 30));
        project2.setStatus(ProjectStatus.Planning);
        project2 = projectRepository.save(project2);

        // 7. Create Project Assignments
        ProjectAssignment assignment1 = new ProjectAssignment();
        assignment1.setProject(project1);
        assignment1.setEmployee(emp1);
        assignment1.setRoleInProject("Developer");
        assignment1.setJoinDate(LocalDate.of(2024, 1, 1));
        projectAssignmentRepository.save(assignment1);

        ProjectAssignment assignment2 = new ProjectAssignment();
        assignment2.setProject(project1);
        assignment2.setEmployee(emp3);
        assignment2.setRoleInProject("Senior Developer");
        assignment2.setJoinDate(LocalDate.of(2024, 1, 5));
        projectAssignmentRepository.save(assignment2);

        ProjectAssignment assignment3 = new ProjectAssignment();
        assignment3.setProject(project1);
        assignment3.setEmployee(emp5);
        assignment3.setRoleInProject("Tester");
        assignment3.setJoinDate(LocalDate.of(2024, 1, 10));
        projectAssignmentRepository.save(assignment3);

        ProjectAssignment assignment4 = new ProjectAssignment();
        assignment4.setProject(project2);
        assignment4.setEmployee(emp1);
        assignment4.setRoleInProject("PM");
        assignment4.setJoinDate(LocalDate.of(2024, 3, 1));
        projectAssignmentRepository.save(assignment4);

        ProjectAssignment assignment5 = new ProjectAssignment();
        assignment5.setProject(project2);
        assignment5.setEmployee(emp2);
        assignment5.setRoleInProject("Business Analyst");
        assignment5.setJoinDate(LocalDate.of(2024, 3, 5));
        projectAssignmentRepository.save(assignment5);

        ProjectAssignment assignment6 = new ProjectAssignment();
        assignment6.setProject(project2);
        assignment6.setEmployee(emp4);
        assignment6.setRoleInProject("Developer");
        assignment6.setJoinDate(LocalDate.of(2024, 3, 10));
        projectAssignmentRepository.save(assignment6);

        System.out.println("============================================");
        System.out.println("Data initialization completed successfully!");
        System.out.println("============================================");
        System.out.println("Test Accounts:");
        System.out.println("  Admin: admin1 / 123456");
        System.out.println("  Admin: admin2 / 123456");
        System.out.println("  Manager: manager1 / 123456");
        System.out.println("  Employee: emp1 / 123456");
        System.out.println("  Employee: emp2 / 123456");
        System.out.println("  Employee: emp3 / 123456");
        System.out.println("  Employee: emp4 / 123456");
        System.out.println("  Employee: emp5 / 123456");
        System.out.println("============================================");
    }

    private Account createAccount(String username, String password, String email, AccountStatus status) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(email);
        account.setStatus(status);
        return account;
    }
}

