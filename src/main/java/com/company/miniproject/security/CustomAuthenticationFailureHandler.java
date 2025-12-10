package com.company.miniproject.security;

import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import com.company.miniproject.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AccountRepository accountRepository;

    @Autowired
    public CustomAuthenticationFailureHandler(AccountRepository accountRepository) {
        super("/login?error=true");
        this.accountRepository = accountRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       AuthenticationException exception) throws IOException, ServletException {
        // Get username from request parameter
        String username = request.getParameter("username");
        
        // Save username to session so it can be retrieved on the login page
        if (username != null && !username.isEmpty()) {
            request.getSession().setAttribute("SPRING_SECURITY_LAST_USERNAME", username);
            
            // Check if account is blocked
            Account account = accountRepository.findByUsername(username).orElse(null);
            if (account != null && account.getStatus() == AccountStatus.Blocked) {
                request.getSession().setAttribute("loginErrorKey", "error.auth.account_blocked");
            } else {
                request.getSession().setAttribute("loginErrorKey", "error.auth.invalid_credentials");
            }
        } else {
            request.getSession().setAttribute("loginErrorKey", "error.auth.invalid_credentials");
        }
        
        // Call parent to handle the redirect
        super.onAuthenticationFailure(request, response, exception);
    }
}

