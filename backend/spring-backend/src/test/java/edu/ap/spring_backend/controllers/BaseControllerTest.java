package edu.ap.spring_backend.controllers;

import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.security.JwtService;
import org.springframework.boot.test.mock.mockito.MockBean;

abstract class BaseControllerTest {

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected UserRepository userRepository;
}