package com.company.miniproject.service.impl;

import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.repository.DepartmentRepository;
import com.company.miniproject.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1);
        department.setName("IT");
        department.setDescription("IT Department");
        department.setEmployees(new HashSet<>());

        employee = new Employee();
        employee.setId(1);
        employee.setFullName("John Doe");
    }

    @Test
    void testFindAll_ShouldReturnAllDepartments() {
        List<Department> departments = Arrays.asList(department);
        when(departmentRepository.findAllWithEmployees()).thenReturn(departments);

        List<Department> result = departmentService.findAll();

        assertEquals(1, result.size());
        verify(departmentRepository).findAllWithEmployees();
    }

    @Test
    void testFindById_WhenExists_ShouldReturnDepartment() {
        when(departmentRepository.findByIdWithEmployees(1)).thenReturn(Optional.of(department));

        Optional<Department> result = departmentService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("IT", result.get().getName());
    }

    @Test
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        when(departmentRepository.findByIdWithEmployees(1)).thenReturn(Optional.empty());

        Optional<Department> result = departmentService.findById(1);

        assertFalse(result.isPresent());
    }

    @Test
    void testSave_WhenNameNotExists_ShouldSave() {
        when(departmentRepository.existsByName("IT")).thenReturn(false);
        when(departmentRepository.save(department)).thenReturn(department);

        Department result = departmentService.save(department);

        assertNotNull(result);
        verify(departmentRepository).save(department);
    }

    @Test
    void testSave_WhenNameExists_ShouldThrowException() {
        when(departmentRepository.existsByName("IT")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            departmentService.save(department);
        });
    }

    @Test
    void testUpdate_WhenExists_ShouldUpdate() {
        Department updatedDept = new Department();
        updatedDept.setName("IT Updated");
        updatedDept.setDescription("Updated Description");

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByName("IT Updated")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department result = departmentService.update(1, updatedDept);

        assertNotNull(result);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void testUpdate_WhenNotExists_ShouldThrowException() {
        Department updatedDept = new Department();
        when(departmentRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            departmentService.update(1, updatedDept);
        });
    }

    @Test
    void testDeleteById_WhenNoEmployees_ShouldDelete() {
        when(departmentRepository.countEmployeesByDepartmentId(1)).thenReturn(0L);
        doNothing().when(departmentRepository).deleteById(1);

        assertDoesNotThrow(() -> departmentService.deleteById(1));
        verify(departmentRepository).deleteById(1);
    }

    @Test
    void testDeleteById_WhenHasEmployees_ShouldThrowException() {
        when(departmentRepository.countEmployeesByDepartmentId(1)).thenReturn(5L);

        assertThrows(IllegalStateException.class, () -> {
            departmentService.deleteById(1);
        });
    }

    @Test
    void testExistsByName_ShouldReturnTrue() {
        when(departmentRepository.existsByName("IT")).thenReturn(true);

        boolean result = departmentService.existsByName("IT");

        assertTrue(result);
    }

    @Test
    void testHasEmployees_WhenHasEmployees_ShouldReturnTrue() {
        when(departmentRepository.countEmployeesByDepartmentId(1)).thenReturn(5L);

        boolean result = departmentService.hasEmployees(1);

        assertTrue(result);
    }

    @Test
    void testAddEmployeeToDepartment_ShouldAddEmployee() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> departmentService.addEmployeeToDepartment(1, 1));
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testAddEmployeeToDepartment_WhenEmployeeAlreadyInDepartment_ShouldThrowException() {
        employee.setDepartment(department);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> {
            departmentService.addEmployeeToDepartment(1, 1);
        });
    }

    @Test
    void testRemoveEmployeeFromDepartment_ShouldRemoveEmployee() {
        employee.setDepartment(department);
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        assertDoesNotThrow(() -> departmentService.removeEmployeeFromDepartment(1, 1));
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testRemoveEmployeeFromDepartment_WhenEmployeeNotInDepartment_ShouldThrowException() {
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        assertThrows(IllegalArgumentException.class, () -> {
            departmentService.removeEmployeeFromDepartment(1, 1);
        });
    }
}






