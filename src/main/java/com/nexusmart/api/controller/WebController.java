package com.nexusmart.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/products")
    public String products() {
        return "products";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
