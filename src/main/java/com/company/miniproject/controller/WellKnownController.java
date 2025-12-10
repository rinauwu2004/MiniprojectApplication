package com.company.miniproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class WellKnownController {

    @RequestMapping("/.well-known/**")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleWellKnown() {
        // Silently ignore .well-known requests (Chrome DevTools, etc.)
    }
}




