package com.company.miniproject.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignmentDto {
    
    // projectId is set from path variable in controller, not from form
    private Integer projectId;
    
    @NotNull(message = "Employee is required")
    private Integer employeeId;
    
    @NotBlank(message = "Role in project is required")
    @Size(min = 2, max = 30, message = "Role in project must be between 2 and 30 characters")
    private String roleInProject;
    
    @NotNull(message = "Join date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDate;
}
