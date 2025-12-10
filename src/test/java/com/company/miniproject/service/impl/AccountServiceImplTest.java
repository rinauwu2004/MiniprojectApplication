package com.company.miniproject.service.impl;

import com.company.miniproject.dto.AccountDto;
import com.company.miniproject.dto.AdminChangePasswordDto;
import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.entity.Role;
import com.company.miniproject.repository.AccountRepository;
import com.company.miniproject.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private Role role;
    private AccountDto accountDto;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ADMIN");

        account = new Account();
        account.setId(1);
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setPassword("$2a$10$encoded");
        account.setStatus(AccountStatus.Active);
        account.setRoles(new HashSet<>(Collections.singletonList(role)));

        accountDto = new AccountDto();
        accountDto.setUsername("newuser");
        accountDto.setEmail("new@example.com");
        accountDto.setPassword("password123");
        accountDto.setStatus(AccountStatus.Active);
        accountDto.setRoleIds(new HashSet<>(Collections.singletonList(1)));
    }

    @Test
    void testFindAll_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(Arrays.asList(account));
        when(accountRepository.findAll(pageable)).thenReturn(page);

        Page<Account> result = accountService.findAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(accountRepository).findAll(pageable);
    }

    @Test
    void testFindById_WhenExists_ShouldReturnAccount() {
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        when(accountRepository.findById(1)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.findById(1);

        assertFalse(result.isPresent());
    }

    @Test
    void testSave_WithValidData_ShouldSave() {
        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        when(accountRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.save(accountDto);

        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testSave_WithEmptyPassword_ShouldThrowException() {
        accountDto.setPassword("");

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.save(accountDto);
        });
    }

    @Test
    void testSave_WithShortPassword_ShouldThrowException() {
        accountDto.setPassword("12345");

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.save(accountDto);
        });
    }

    @Test
    void testSave_WithExistingUsername_ShouldThrowException() {
        when(accountRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.save(accountDto);
        });
    }

    @Test
    void testUpdate_WithValidData_ShouldUpdate() {
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        account.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(accountRepository.existsByUsername("newuser")).thenReturn(false);
        when(accountRepository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.update(1, accountDto);

        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testUpdate_WithAdminAccount_ShouldOnlyUpdateStatus() {
        accountDto.setStatus(AccountStatus.Blocked);
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.update(1, accountDto);

        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testDeleteById_WithNonAdminAccount_ShouldDelete() {
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        account.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).deleteById(1);

        assertDoesNotThrow(() -> accountService.deleteById(1));
        verify(accountRepository).deleteById(1);
    }

    @Test
    void testDeleteById_WithAdminAccount_ShouldThrowException() {
        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> {
            accountService.deleteById(1);
        });
    }

    @Test
    void testChangePassword_WithValidData_ShouldUpdate() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldpass", account.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newpass", account.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newencoded");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        assertDoesNotThrow(() -> accountService.changePassword(1, dto));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testChangePassword_WithWrongCurrentPassword_ShouldThrowException() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword("wrongpass");
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongpass", account.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.changePassword(1, dto);
        });
    }

    @Test
    void testAdminChangePassword_WithValidData_ShouldUpdate() {
        AdminChangePasswordDto dto = new AdminChangePasswordDto();
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        account.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newencoded");
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        assertDoesNotThrow(() -> accountService.adminChangePassword(1, dto));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testAdminChangePassword_WithAdminAccount_ShouldThrowException() {
        AdminChangePasswordDto dto = new AdminChangePasswordDto();
        dto.setNewPassword("newpass");
        dto.setConfirmPassword("newpass");

        when(accountRepository.findById(1)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> {
            accountService.adminChangePassword(1, dto);
        });
    }
}

