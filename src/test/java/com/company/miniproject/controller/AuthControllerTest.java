package com.company.miniproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testShowLoginPage_WithNoParams_ShouldReturnLogin() {
        String result = authController.showLoginPage(null, null, request, model);
        assertEquals("login", result);
    }

    @Test
    void testShowLoginPage_WithError_ShouldAddErrorToModel() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginErrorKey")).thenReturn("error.auth.invalid_credentials");
        when(session.getAttribute("SPRING_SECURITY_LAST_USERNAME")).thenReturn("testuser");

        String result = authController.showLoginPage("true", null, request, model);

        assertEquals("login", result);
        verify(model).addAttribute("loginErrorKey", "error.auth.invalid_credentials");
        verify(model).addAttribute("lastUsername", "testuser");
        verify(session).removeAttribute("loginErrorKey");
    }

    @Test
    void testShowLoginPage_WithErrorAndNoSession_ShouldAddDefaultError() {
        when(request.getSession(false)).thenReturn(null);

        String result = authController.showLoginPage("true", null, request, model);

        assertEquals("login", result);
        verify(model).addAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }

    @Test
    void testShowLoginPage_WithErrorAndNoErrorKey_ShouldAddDefaultError() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginErrorKey")).thenReturn(null);

        String result = authController.showLoginPage("true", null, request, model);

        assertEquals("login", result);
        verify(model).addAttribute("loginErrorKey", "error.auth.invalid_credentials");
    }

    @Test
    void testShowLoginPage_WithLogout_ShouldAddLogoutSuccess() {
        String result = authController.showLoginPage(null, "true", request, model);

        assertEquals("login", result);
        verify(model).addAttribute("logoutSuccessKey", "auth.logout.success");
    }
}




