package com.company.miniproject.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("\nUse this hash in your data.sql file:");
        System.out.println("'" + hash + "'");

        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + (matches ? "SUCCESS" : "FAILED"));
    }
}




