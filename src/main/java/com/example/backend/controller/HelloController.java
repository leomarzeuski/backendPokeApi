package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Olá, LEOOOOOOO!";
    }

    @GetMapping("/api/status")
    public String apiStatus() {
        return "PokeAPI integration is ready!";
    }
}