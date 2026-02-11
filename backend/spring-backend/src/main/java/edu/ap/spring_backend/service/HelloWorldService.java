package edu.ap.spring_backend.service;

import edu.ap.spring_backend.DAO.HelloWorldRepository;
import edu.ap.spring_backend.entity.Hello;
import org.springframework.stereotype.Service;

@Service
public class HelloWorldService {
    private HelloWorldRepository helloWorldRepository;

    public HelloWorldService(HelloWorldRepository helloWorldRepository) {
        this.helloWorldRepository = helloWorldRepository;
    }

    public Hello getHelloWorld() {
        return helloWorldRepository.findById(1).get();
    }
}
