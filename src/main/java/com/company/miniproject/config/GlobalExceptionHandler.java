package com.company.miniproject.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "Page not found");
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error/404";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error/400";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request, Model model) {
        String errorMessage = "Data integrity violation occurred";
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
            String message = rootCause.getMessage();
            if (message != null) {
                if (message.contains("username") || message.contains("uk_account_username")) {
                    errorMessage = "Username already exists. Please choose a different username.";
                } else if (message.contains("email") || message.contains("uk_account_email")) {
                    errorMessage = "Email already exists. This email is already used by another user. Please use a different email address.";
                } else if (message.contains("phone") || message.contains("uk_employee_phone")) {
                    errorMessage = "Phone number already exists. Please use a different phone number.";
                }
            }
        }
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "An internal server error occurred");
        model.addAttribute("requestedUrl", request.getRequestURL());
        ex.printStackTrace();
        return "error/500";
    }
}

