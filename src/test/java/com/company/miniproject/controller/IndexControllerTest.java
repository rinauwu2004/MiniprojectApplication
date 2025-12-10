package com.company.miniproject.controller;

import com.company.miniproject.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private IndexController indexController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testIndex_WhenNotAuthenticated_ShouldRedirectToLogin() {
        String result = indexController.index(null, model);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testIndex_WhenAuthenticatedAsAdmin_ShouldReturnIndex() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", 10L);
        stats.put("totalDepartments", 3L);
        stats.put("totalEmployees", 5L);
        stats.put("totalProjects", 2L);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(dashboardService.getStatistics(authentication)).thenReturn(stats);

        String result = indexController.index(authentication, model);

        assertEquals("index", result);
        verify(model).addAttribute("stats", stats);
        verify(model).addAttribute("isAdmin", true);
        verify(model).addAttribute("isManager", false);
        verify(model).addAttribute("isEmployee", false);
    }

    @Test
    void testIndex_WhenAuthenticatedAsManager_ShouldReturnIndex() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_MANAGER")
        );
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalEmployees", 5L);
        stats.put("totalDepartments", 3L);
        stats.put("totalProjects", 2L);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(dashboardService.getStatistics(authentication)).thenReturn(stats);

        String result = indexController.index(authentication, model);

        assertEquals("index", result);
        verify(model).addAttribute("stats", stats);
        verify(model).addAttribute("isAdmin", false);
        verify(model).addAttribute("isManager", true);
        verify(model).addAttribute("isEmployee", false);
    }

    @Test
    void testIndex_WhenAuthenticatedAsEmployee_ShouldReturnIndex() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_EMPLOYEE")
        );
        Map<String, Long> stats = new HashMap<>();
        stats.put("assignedProjects", 2L);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(dashboardService.getStatistics(authentication)).thenReturn(stats);

        String result = indexController.index(authentication, model);

        assertEquals("index", result);
        verify(model).addAttribute("stats", stats);
        verify(model).addAttribute("isAdmin", false);
        verify(model).addAttribute("isManager", false);
        verify(model).addAttribute("isEmployee", true);
    }

    @Test
    void testIndexPage_ShouldCallIndex() {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        Map<String, Long> stats = new HashMap<>();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn((Collection)authorities);
        when(dashboardService.getStatistics(authentication)).thenReturn(stats);

        String result = indexController.indexPage(authentication, model);

        assertEquals("index", result);
    }
}

