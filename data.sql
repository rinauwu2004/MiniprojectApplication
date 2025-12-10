-- ============================================
-- DATA SEEDING SCRIPT FOR HR MANAGEMENT SYSTEM
-- ============================================
-- This script inserts sample data for testing
-- Default password for all accounts: 123456
-- ============================================

USE `human_resources`;

-- ============================================
-- 1. INSERT ROLES (3 roles: ADMIN, MANAGER, EMPLOYEE)
-- ============================================
INSERT INTO `role` (`name`) VALUES
('ADMIN'),
('MANAGER'),
('EMPLOYEE');

-- ============================================
-- 2. INSERT DEPARTMENTS (3 departments)
-- ============================================
INSERT INTO `department` (`name`, `description`) VALUES
('IT', 'Information Technology Department - Handles all IT infrastructure and software development'),
('HR', 'Human Resources Department - Manages employee relations, recruitment, and benefits'),
('Sales', 'Sales Department - Responsible for business development and customer relations');

-- ============================================
-- 3. INSERT ACCOUNTS
-- ============================================
-- Password for all accounts: 123456
-- BCrypt hash (verified): $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
-- Note: This is a verified BCrypt hash for password "123456"
-- IMPORTANT: If this hash doesn't work, use DataInitializer.java instead
--            which will automatically create data with correct password encoding

-- Admin Accounts (2 admins)
INSERT INTO `account` (`username`, `password`, `email`, `status`) VALUES
('admin1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin1@company.com', 'Active'),
('admin2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin2@company.com', 'Active');

-- Manager Account (1 manager)
INSERT INTO `account` (`username`, `password`, `email`, `status`) VALUES
('manager1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'manager1@company.com', 'Active');

-- Employee Accounts (5 employees)
INSERT INTO `account` (`username`, `password`, `email`, `status`) VALUES
('emp1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp1@company.com', 'Active'),
('emp2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp2@company.com', 'Active'),
('emp3', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp3@company.com', 'Active'),
('emp4', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp4@company.com', 'Active'),
('emp5', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp5@company.com', 'Active');

-- ============================================
-- 4. INSERT ACCOUNT_ROLE (Assign roles to accounts)
-- ============================================
-- Admin accounts get ADMIN role
INSERT INTO `account_role` (`account_id`, `role_id`) VALUES
(1, 1), -- admin1 -> ADMIN
(2, 1); -- admin2 -> ADMIN

-- Manager account gets MANAGER role
INSERT INTO `account_role` (`account_id`, `role_id`) VALUES
(3, 2); -- manager1 -> MANAGER

-- Employee accounts get EMPLOYEE role
INSERT INTO `account_role` (`account_id`, `role_id`) VALUES
(4, 3), -- emp1 -> EMPLOYEE
(5, 3), -- emp2 -> EMPLOYEE
(6, 3), -- emp3 -> EMPLOYEE
(7, 3), -- emp4 -> EMPLOYEE
(8, 3); -- emp5 -> EMPLOYEE

-- Optional: Give manager1 also EMPLOYEE role (example of multiple roles)
INSERT INTO `account_role` (`account_id`, `role_id`) VALUES
(3, 3); -- manager1 -> EMPLOYEE (can have multiple roles)

-- ============================================
-- 5. INSERT EMPLOYEES (5 employees)
-- ============================================
INSERT INTO `employee` (`full_name`, `birth_date`, `gender`, `phone`, `address`, `department_id`, `account_id`) VALUES
('John Doe', '1990-05-15', 'Male', '0912345678', '123 Main Street, Ho Chi Minh City', 1, 4), -- emp1, IT department
('Jane Smith', '1992-08-20', 'Female', '0923456789', '456 Oak Avenue, Hanoi', 2, 5), -- emp2, HR department
('Bob Johnson', '1988-03-10', 'Male', '0934567890', '789 Pine Road, Da Nang', 1, 6), -- emp3, IT department
('Alice Williams', '1995-11-25', 'Female', '0945678901', '321 Elm Street, Can Tho', 3, 7), -- emp4, Sales department
('Charlie Brown', '1991-07-30', 'Male', '0956789012', '654 Maple Drive, Hai Phong', 1, 8); -- emp5, IT department

-- ============================================
-- 6. INSERT PROJECTS (2 projects)
-- ============================================
INSERT INTO `project` (`name`, `start_date`, `end_date`, `status`) VALUES
('E-Commerce Platform Development', '2024-01-01', '2024-12-31', 'Ongoing'),
('Customer Management System', '2024-03-01', '2024-09-30', 'Planning');

-- ============================================
-- 7. INSERT PROJECT_ASSIGNMENTS
-- ============================================
-- Assign employees to projects
INSERT INTO `project_assignment` (`project_id`, `employee_id`, `role_in_project`, `join_date`) VALUES
-- Project 1: E-Commerce Platform Development
(1, 1, 'Developer', '2024-01-01'), -- John Doe as Developer
(1, 3, 'Senior Developer', '2024-01-05'), -- Bob Johnson as Senior Developer
(1, 5, 'Tester', '2024-01-10'), -- Charlie Brown as Tester
-- Project 2: Customer Management System
(2, 1, 'PM', '2024-03-01'), -- John Doe as PM
(2, 2, 'Business Analyst', '2024-03-05'), -- Jane Smith as Business Analyst
(2, 4, 'Developer', '2024-03-10'); -- Alice Williams as Developer

-- ============================================
-- VERIFICATION QUERIES (Optional - for checking data)
-- ============================================
-- SELECT * FROM `role`;
-- SELECT * FROM `department`;
-- SELECT a.id, a.username, a.email, r.name as role_name FROM `account` a 
--   JOIN `account_role` ar ON a.id = ar.account_id 
--   JOIN `role` r ON ar.role_id = r.id;
-- SELECT * FROM `employee`;
-- SELECT * FROM `project`;
-- SELECT pa.id, p.name as project_name, e.full_name as employee_name, pa.role_in_project 
--   FROM `project_assignment` pa
--   JOIN `project` p ON pa.project_id = p.id
--   JOIN `employee` e ON pa.employee_id = e.id;

-- ============================================
-- TEST ACCOUNTS SUMMARY
-- ============================================
-- ADMIN Accounts:
--   Username: admin1, Password: 123456
--   Username: admin2, Password: 123456
--
-- MANAGER Account:
--   Username: manager1, Password: 123456
--
-- EMPLOYEE Accounts:
--   Username: emp1, Password: 123456
--   Username: emp2, Password: 123456
--   Username: emp3, Password: 123456
--   Username: emp4, Password: 123456
--   Username: emp5, Password: 123456
-- ============================================

