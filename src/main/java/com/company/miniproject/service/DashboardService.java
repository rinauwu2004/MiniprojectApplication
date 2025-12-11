package com.company.miniproject.service;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface DashboardService {
    
    Map<String, Long> getStatistics(Authentication authentication);
    
    Map<String, Object> getAdminDashboardData();
    
    Map<String, Object> getManagerDashboardData(Authentication authentication);
    
    Map<String, Object> getEmployeeDashboardData(Authentication authentication);
}

