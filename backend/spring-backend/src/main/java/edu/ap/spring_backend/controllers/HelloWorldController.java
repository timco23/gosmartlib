package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.service.HelloWorldService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost"}) 
public class HelloWorldController {
    // test controller

    private HelloWorldService helloWorldService;
    public HelloWorldController (HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok(helloWorldService.getHelloWorld().getContent());
    }
}
    