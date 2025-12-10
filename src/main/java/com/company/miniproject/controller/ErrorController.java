package com.company.miniproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    public String error403(Model model) {
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "Access Denied. You don't have permission to access this resource.");
        return "error/403";
    }
}

