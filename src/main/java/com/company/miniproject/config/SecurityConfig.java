package com.company.miniproject.config;

import com.company.miniproject.security.CustomAuthenticationFailureHandler;
import com.company.miniproject.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/error/**", "/favicon.ico", "/.well-known/**").permitAll()
                        // ADMIN: Toàn quyền hệ thống (CRUD User, Department, Project, Employee)
                        .requestMatchers("/accounts/**").hasRole("ADMIN")
                        // MANAGER: Có thể xem danh sách phòng ban và chi tiết, nhưng không thể CRUD
                        .requestMatchers("/departments/new", "/departments/*/edit", "/departments/*/delete").hasRole("ADMIN")
                        .requestMatchers("/departments", "/departments/*").hasAnyRole("ADMIN", "MANAGER")
                        // Projects: ADMIN và MANAGER có quyền quản lý, EMPLOYEE chỉ xem được projects họ được assign
                        // Specific patterns first (more specific = higher priority)
                        .requestMatchers("/projects/new").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/projects/assignments/*/delete").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/projects/*/edit").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/projects/*/delete").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/projects/*/assignments").hasAnyRole("ADMIN", "MANAGER")
                        // General patterns (less specific = lower priority)
                        .requestMatchers("/projects").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers("/projects/*").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        // MANAGER: Quản lý Dự án, Nhân viên, xem danh sách Phòng ban
                        // MANAGER không được đổi mật khẩu của Employee (chỉ ADMIN)
                        .requestMatchers("/employees/*/change-password").hasRole("ADMIN")
                        .requestMatchers("/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/manager/projects/**", "/manager/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/manager/departments/**").hasAnyRole("ADMIN", "MANAGER")
                        // EMPLOYEE: Chỉ xem thông tin cá nhân và dự án được phân công
                        .requestMatchers("/employee/profile/**", "/employee/projects/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        // Profile page - accessible to all authenticated users
                        .requestMatchers("/profile/**").authenticated()
                        // Root và index page - cần authenticate
                        .requestMatchers("/", "/index").authenticated()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler(authenticationFailureHandler)
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
