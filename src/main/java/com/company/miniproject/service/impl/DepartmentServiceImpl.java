package com.company.miniproject.service.impl;

import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.repository.DepartmentRepository;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        List<Department> departments = departmentRepository.findAllWithEmployees();
        for (Department dept : departments) {
            if (dept.getEmployees() != null) {
                dept.getEmployees().size(); // Force initialization
            }
        }
        return departments;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> findById(Integer id) {
        return departmentRepository.findByIdWithEmployees(id);
    }

    @Override
    public Department save(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("Department with name '" + department.getName() + "' already exists");
        }
        return departmentRepository.save(department);
    }

    @Override
    public Department update(Integer id, Department department) {
        Department existingDept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        
        if (!existingDept.getName().equals(department.getName()) && 
            departmentRepository.existsByName(department.getName())) {
            throw new IllegalArgumentException("Department with name '" + department.getName() + "' already exists");
        }
        
        existingDept.setName(department.getName());
        existingDept.setDescription(department.getDescription());
        
        return departmentRepository.save(existingDept);
    }

    @Override
    public void deleteById(Integer id) throws IllegalStateException {
        if (hasEmployees(id)) {
            throw new IllegalStateException("Cannot delete department. It still has employees.");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployees(Integer departmentId) {
        return departmentRepository.countEmployeesByDepartmentId(departmentId) > 0;
    }
    
    @Override
    public void addEmployeeToDepartment(Integer departmentId, Integer employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        
        if (employee.getDepartment() != null && employee.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Employee is already in this department");
        }
        
        Department oldDepartment = employee.getDepartment();
        if (oldDepartment != null) {
            oldDepartment.getEmployees().remove(employee);
        }
        
        employee.setDepartment(department);
        employeeRepository.save(employee);
    }
    
    @Override
    public void removeEmployeeFromDepartment(Integer departmentId, Integer employeeId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
        
        if (employee.getDepartment() == null || !employee.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Employee is not in this department");
        }
        
        department.getEmployees().remove(employee);
        employee.setDepartment(null);
        employeeRepository.save(employee);
    }
}

