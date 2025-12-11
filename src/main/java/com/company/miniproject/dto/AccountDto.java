package com.company.miniproject.dto;

import com.company.miniproject.entity.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    
    private Integer id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password; // Optional for update
    
    @NotNull(message = "Status is required")
    private AccountStatus status;
    
    @NotNull(message = "At least one role is required")
    private Set<Integer> roleIds; // Role IDs selected by admin
}







