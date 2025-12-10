package com.company.miniproject.controller;

import com.company.miniproject.entity.Department;
import com.company.miniproject.entity.Employee;
import com.company.miniproject.repository.EmployeeRepository;
import com.company.miniproject.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private DepartmentController departmentController;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1);
        department.setName("IT");
        department.setDescription("IT Department");
    }

    @Test
    void testListDepartments_ShouldReturnList() {
        List<Department> departments = Arrays.asList(department);
        when(departmentService.findAll()).thenReturn(departments);

        String result = departmentController.listDepartments(model);

        assertEquals("department/list", result);
        verify(model).addAttribute("departments", departments);
    }

    @Test
    void testListDepartments_WithException_ShouldReturnListWithError() {
        when(departmentService.findAll()).thenThrow(new RuntimeException("Database error"));

        String result = departmentController.listDepartments(model);

        assertEquals("department/list", result);
        verify(model).addAttribute(eq("errorMessage"), anyString());
    }

    @Test
    void testShowDepartmentDetail_ShouldReturnDetail() {
        List<Employee> availableEmployees = Arrays.asList();
        when(departmentService.findById(1)).thenReturn(java.util.Optional.of(department));
        when(employeeRepository.findEmployeesNotInDepartment(1)).thenReturn(availableEmployees);

        String result = departmentController.showDepartmentDetail(1, model);

        assertEquals("department/detail", result);
        verify(model).addAttribute("department", department);
        verify(model).addAttribute("availableEmployees", availableEmployees);
    }

    @Test
    void testShowCreateForm_ShouldReturnForm() {
        String result = departmentController.showCreateForm(model);

        assertEquals("department/form", result);
        verify(model).addAttribute(eq("department"), any(Department.class));
        verify(model).addAttribute("formAction", "/departments");
    }

    @Test
    void testCreateDepartment_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(departmentService.save(any(Department.class))).thenReturn(department);

        String result = departmentController.createDepartment(department, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/departments", result);
        verify(departmentService).save(department);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Department created successfully");
    }

    @Test
    void testCreateDepartment_WithErrors_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = departmentController.createDepartment(department, bindingResult, model, redirectAttributes);

        assertEquals("department/form", result);
        verify(departmentService, never()).save(any());
    }

    @Test
    void testCreateDepartment_WithException_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(departmentService.save(any(Department.class))).thenThrow(new IllegalArgumentException("Name exists"));

        String result = departmentController.createDepartment(department, bindingResult, model, redirectAttributes);

        assertEquals("department/form", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Name exists");
    }

    @Test
    void testShowEditForm_ShouldReturnForm() {
        when(departmentService.findById(1)).thenReturn(java.util.Optional.of(department));

        String result = departmentController.showEditForm(1, model);

        assertEquals("department/form", result);
        verify(model).addAttribute("department", department);
        verify(model).addAttribute("formAction", "/departments/1");
    }

    @Test
    void testUpdateDepartment_WithValidData_ShouldRedirect() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(departmentService.update(eq(1), any(Department.class))).thenReturn(department);

        String result = departmentController.updateDepartment(1, department, bindingResult, model, redirectAttributes);

        assertEquals("redirect:/departments", result);
        verify(departmentService).update(1, department);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Department updated successfully");
    }

    @Test
    void testUpdateDepartment_WithErrors_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String result = departmentController.updateDepartment(1, department, bindingResult, model, redirectAttributes);

        assertEquals("department/form", result);
        verify(departmentService, never()).update(anyInt(), any());
    }

    @Test
    void testDeleteDepartment_ShouldRedirect() {
        doNothing().when(departmentService).deleteById(1);

        String result = departmentController.deleteDepartment(1, redirectAttributes);

        assertEquals("redirect:/departments", result);
        verify(departmentService).deleteById(1);
    }

    @Test
    void testDeleteDepartment_WithException_ShouldRedirect() {
        doThrow(new IllegalStateException("Has employees")).when(departmentService).deleteById(1);

        String result = departmentController.deleteDepartment(1, redirectAttributes);

        assertEquals("redirect:/departments", result);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Has employees");
    }

    @Test
    void testAddEmployeeToDepartment_ShouldRedirect() {
        doNothing().when(departmentService).addEmployeeToDepartment(1, 2);

        String result = departmentController.addEmployeeToDepartment(1, 2, redirectAttributes);

        assertEquals("redirect:/departments/1", result);
        verify(departmentService).addEmployeeToDepartment(1, 2);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Employee added to department successfully");
    }

    @Test
    void testRemoveEmployeeFromDepartment_ShouldRedirect() {
        doNothing().when(departmentService).removeEmployeeFromDepartment(1, 2);

        String result = departmentController.removeEmployeeFromDepartment(1, 2, redirectAttributes);

        assertEquals("redirect:/departments/1", result);
        verify(departmentService).removeEmployeeFromDepartment(1, 2);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Employee removed from department successfully");
    }
}

