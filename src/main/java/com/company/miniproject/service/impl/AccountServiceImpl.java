package com.company.miniproject.service.impl;

import com.company.miniproject.dto.AccountDto;
import com.company.miniproject.dto.AdminChangePasswordDto;
import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.Role;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.RoleRepository;
import com.company.miniproject.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> searchAccounts(String keyword, Integer roleId, com.company.miniproject.entity.AccountStatus status, Pageable pageable) {
        return accountRepository.searchAccounts(keyword, roleId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(Integer id) {
        return accountRepository.findById(id);
    }

    @Override
    public Account save(AccountDto dto) {
        // Validate password for create
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Validate unique constraints with detailed error messages
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }
        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists. Please use a different email address.");
        }
        
        // Create account
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setEmail(dto.getEmail());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setStatus(dto.getStatus());
        
        // Set roles
        Set<Role> roles = new HashSet<>();
        for (Integer roleId : dto.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
            roles.add(role);
        }
        account.setRoles(roles);
        
        return accountRepository.save(account);
    }

    @Override
    public Account update(Integer id, AccountDto dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        // Check if account has ADMIN role
        boolean isAdminAccount = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        
        // Validate unique constraints for update (excluding current account) with detailed error messages
        if (!account.getUsername().equals(dto.getUsername()) && accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }
        if (!account.getEmail().equals(dto.getEmail()) && accountRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists. Please use a different email address.");
        }
        
        // For admin accounts, allow status update (admin can edit status of other admin accounts)
        if (isAdminAccount) {
            account.setStatus(dto.getStatus());
            // Do not update username, email, roles, or password for admin accounts
            // Only status can be changed
        } else {
            // For non-admin accounts, allow full update
            account.setUsername(dto.getUsername());
            account.setEmail(dto.getEmail());
            account.setStatus(dto.getStatus());
            
            // Update password only if provided
            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                if (dto.getPassword().length() < 6) {
                    throw new IllegalArgumentException("Password must be at least 6 characters");
                }
                account.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            
            // Update roles
            Set<Role> roles = new HashSet<>();
            for (Integer roleId : dto.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
                roles.add(role);
            }
            account.setRoles(roles);
        }
        
        return accountRepository.save(account);
    }

    @Override
    public void deleteById(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        
        // Check if account has ADMIN role - admin cannot delete other admin accounts
        boolean isAdminAccount = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        if (isAdminAccount) {
            throw new IllegalStateException("Cannot delete admin accounts. Admin accounts can only be viewed.");
        }
        
        accountRepository.deleteById(id);
    }

    @Override
    public void changePassword(Integer accountId, ChangePasswordDto dto) {
        // Find account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));
        
        // Validate current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password matches confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Validate new password is different from current password
        if (passwordEncoder.matches(dto.getNewPassword(), account.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        
        // Update password - IMPORTANT: Only hash once!
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    public void adminChangePassword(Integer accountId, AdminChangePasswordDto dto) {
        // Find account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));
        
        // Check if account has ADMIN role - admin cannot change password of other admin accounts
        boolean isAdminAccount = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));
        if (isAdminAccount) {
            throw new IllegalStateException("Cannot change password for admin accounts. Admin accounts can only be viewed.");
        }
        
        // Validate new password matches confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        // Update password - Admin can change password without knowing current password
        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
}

