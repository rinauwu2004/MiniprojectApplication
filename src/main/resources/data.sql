INSERT INTO `role` (`name`) VALUES
('ADMIN'),
('MANAGER'),
('EMPLOYEE');

INSERT INTO `department` (`name`, `description`) VALUES
('IT', 'Information Technology Department - Handles all IT infrastructure and software development'),
('HR', 'Human Resources Department - Manages employee relations, recruitment, and benefits'),
('Sales', 'Sales Department - Responsible for business development and customer relations');

INSERT INTO `account` (`username`, `password`, `email`, `status`) VALUES
('admin1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin1@company.com', 'Active'),
('admin2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin2@company.com', 'Active'),
('manager1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'manager1@company.com', 'Active'),
('emp1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp1@company.com', 'Active'),
('emp2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp2@company.com', 'Active'),
('emp3', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp3@company.com', 'Active'),
('emp4', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp4@company.com', 'Active'),
('emp5', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'emp5@company.com', 'Active');

INSERT INTO `account_role` (`account_id`, `role_id`) VALUES
(1, 1),
(2, 1),
(3, 2),
(3, 3),
(4, 3),
(5, 3),
(6, 3),
(7, 3),
(8, 3);

INSERT INTO `employee` (`full_name`, `birth_date`, `gender`, `phone`, `address`, `department_id`, `account_id`) VALUES
('John Doe', '1990-05-15', 'Male', '0912345678', '123 Main Street, Ho Chi Minh City', 1, 4),
('Jane Smith', '1992-08-20', 'Female', '0923456789', '456 Oak Avenue, Hanoi', 2, 5),
('Bob Johnson', '1988-03-10', 'Male', '0934567890', '789 Pine Road, Da Nang', 1, 6),
('Alice Williams', '1995-11-25', 'Female', '0945678901', '321 Elm Street, Can Tho', 3, 7),
('Charlie Brown', '1991-07-30', 'Male', '0956789012', '654 Maple Drive, Hai Phong', 1, 8);

INSERT INTO `project` (`name`, `start_date`, `end_date`, `status`) VALUES
('E-Commerce Platform Development', '2024-01-01', '2024-12-31', 'Ongoing'),
('Customer Management System', '2024-03-01', '2024-09-30', 'Planning');

INSERT INTO `project_assignment` (`project_id`, `employee_id`, `role_in_project`, `join_date`) VALUES
(1, 1, 'DEVELOPER', '2024-01-01'),
(1, 3, 'SENIOR DEVELOPER', '2024-01-05'),
(1, 5, 'TESTER', '2024-01-10'),
(2, 1, 'PM', '2024-03-01'),
(2, 2, 'BUSINESS ANALYST', '2024-03-05'),
(2, 4, 'DEVELOPER', '2024-03-10');
