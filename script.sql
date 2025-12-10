create schema `human_resources`;
use `human_resources`;


-- SECURITY
create table `account` (
	`id` int auto_increment,
	`username` varchar(50) not null,
    `password` varchar(255) not null,
    `email` varchar(255) not null,
    `status` enum('Active', 'Blocked') not null,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_username` (`username`),
    UNIQUE KEY `uk_account_email` (`email`)
);

create table `role`(
	`id` int auto_increment,
    `name` varchar(100) not null,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_name` (`name`)
);

create table `account_role`(
	`account_id` int not null,
    `role_id` int not null,
    
    PRIMARY KEY (`account_id`, `role_id`),
    FOREIGN KEY (`account_id`) 
		REFERENCES `account`(`id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (`role_id`)
		REFERENCES `role`(`id`) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- HUMAN RESOURCE
create table `department` (
	`id` int auto_increment,
    `name` varchar(50) not null,
    `description` text,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_name` (`name`)
);

create table `employee` (
	`id` int auto_increment,
    `full_name` varchar(255) not null,
    `birth_date` date not null,
    `gender` enum ('Male', 'Female', 'Other') not null,
    `phone` varchar(30) not null,
    `address` varchar(255),
    `department_id` int,
    `account_id` int not null,
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_employee_phone` (`phone`),
    UNIQUE KEY `uk_employee_account_id` (`account_id`),
    FOREIGN KEY (`department_id`)
		REFERENCES `department` (`id`)
        ON DELETE SET NULL ON UPDATE CASCADE,
	FOREIGN KEY (`account_id`)
		REFERENCES `account` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- PROJECT
create table `project` (
	`id` int auto_increment,
    `name` varchar(100) not null,
    `start_date` date,
    `end_date` date,
    `status` enum ('Planning', 'Ongoing', 'Completed'),
    
    PRIMARY KEY (`id`)
);

create table `project_assignment` (
	`id` int auto_increment,
    `project_id` int not null,
    `employee_id` int not null,
    `role_in_project` varchar(30) not null,
	`join_date` date default(CURRENT_DATE()),
    
    PRIMARY KEY (`id`),
    FOREIGN KEY (`project_id`)
		REFERENCES `project` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (`employee_id`)
		REFERENCES `employee` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
	UNIQUE KEY `uk_project_employee` (`project_id`, `employee_id`)
);
