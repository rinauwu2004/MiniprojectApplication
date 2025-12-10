package com.company.miniproject.controller;

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
class ErrorControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private ErrorController errorController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testError403_ShouldReturnError403View() {
        String result = errorController.error403(model);

        assertEquals("error/403", result);
        verify(model).addAttribute("errorCode", "403");
        verify(model).addAttribute("errorMessage", "Access Denied. You don't have permission to access this resource.");
    }
}


