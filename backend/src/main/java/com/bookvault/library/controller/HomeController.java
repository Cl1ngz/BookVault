package com.bookvault.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToSwagger() {
        // Przekierowanie na adres Swaggera
        return "redirect:/swagger-ui/index.html";
    }
}