package edu.ap.spring_backend.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200") 
public class HelloWorldController {
    // test controller

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
}
