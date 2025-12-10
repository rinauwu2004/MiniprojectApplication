package com.company.miniproject.security;

import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.entity.Role;
import com.company.miniproject.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Account account;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ADMIN");

        account = new Account();
        account.setId(1);
        account.setUsername("testuser");
        account.setPassword("$2a$10$encodedPassword");
        account.setEmail("test@example.com");
        account.setStatus(AccountStatus.Active);
        account.setRoles(new HashSet<>(Collections.singletonList(role)));
    }

    @Test
    void testLoadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        when(accountRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void testLoadUserByUsername_WhenAccountBlocked_ShouldReturnLockedUser() {
        account.setStatus(AccountStatus.Blocked);
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertFalse(userDetails.isAccountNonLocked());
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_WithMultipleRoles_ShouldReturnAllRoles() {
        Role managerRole = new Role();
        managerRole.setId(2);
        managerRole.setName("MANAGER");
        account.getRoles().add(managerRole);

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }
}

