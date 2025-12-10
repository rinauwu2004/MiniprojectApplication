package com.company.miniproject.security;

import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFailureHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private AuthenticationException exception;

    @InjectMocks
    private CustomAuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testOnAuthenticationFailure_WithBlockedAccount_ShouldSetBlockedError() throws ServletException, IOException {
        Account account = new Account();
        account.setUsername("testuser");
        account.setStatus(AccountStatus.Blocked);

        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getSession()).thenReturn(session);
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute("SPRING_SECURITY_LAST_USERNAME", "testuser");
        verify(session).setAttribute("loginErrorKey", "error.auth.account_blocked");
    }

    @Test
    void testOnAuthenticationFailure_WithActiveAccount_ShouldSetInvalidCredentials() throws ServletException, IOException {
        Account account = new Account();
        account.setUsername("testuser");
        account.setStatus(AccountStatus.Active);

        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getSession()).thenReturn(session);
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute("SPRING_SECURITY_LAST_USERNAME", "testuser");
        verify(session).setAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }

    @Test
    void testOnAuthenticationFailure_WithNoUsername_ShouldSetInvalidCredentials() throws ServletException, IOException {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }

    @Test
    void testOnAuthenticationFailure_WithEmptyUsername_ShouldSetInvalidCredentials() throws ServletException, IOException {
        when(request.getParameter("username")).thenReturn("");
        when(request.getSession()).thenReturn(session);

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }

    @Test
    void testOnAuthenticationFailure_WithNonExistentUser_ShouldSetInvalidCredentials() throws ServletException, IOException {
        when(request.getParameter("username")).thenReturn("nonexistent");
        when(request.getSession()).thenReturn(session);
        when(accountRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        handler.onAuthenticationFailure(request, response, exception);

        verify(session).setAttribute("SPRING_SECURITY_LAST_USERNAME", "nonexistent");
        verify(session).setAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }
}

