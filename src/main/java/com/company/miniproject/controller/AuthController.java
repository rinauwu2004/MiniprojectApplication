package com.company.miniproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpServletRequest request,
            Model model) {

        if (error != null) {
            // Get error key from session (set by CustomAuthenticationFailureHandler)
            HttpSession session = request.getSession(false);
            if (session != null) {
                String loginErrorKey = (String) session.getAttribute("loginErrorKey");
                if (loginErrorKey != null) {
                    model.addAttribute("loginErrorKey", loginErrorKey);
                    session.removeAttribute("loginErrorKey"); // Remove after use
                } else {
                    model.addAttribute("loginErrorKey", "error.auth.invalid_credentials");
                }
                
                // Get username from session
                String lastUsername = (String) session.getAttribute("SPRING_SECURITY_LAST_USERNAME");
                if (lastUsername != null) {
                    model.addAttribute("lastUsername", lastUsername);
                }
            } else {
                model.addAttribute("loginErrorKey", "error.auth.invalid_credentials");
            }
        }

        if (logout != null) {
            model.addAttribute("logoutSuccessKey", "auth.logout.success");
        }

        return "login";
    }
}
