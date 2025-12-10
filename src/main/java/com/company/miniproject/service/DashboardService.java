package com.company.miniproject.service;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface DashboardService {
    
    Map<String, Long> getStatistics(Authentication authentication);
}

