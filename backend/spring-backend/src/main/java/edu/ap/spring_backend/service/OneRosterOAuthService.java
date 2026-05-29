package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.OneRosterUserDTO;
import edu.ap.spring_backend.dto.OneRosterUserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OneRosterOAuthService {
    private WebClient webClient;

    @Value("${smartschool.platform}")
    private String baseUrl;
    public OneRosterOAuthService(WebClient webClient) {
        this.webClient = webClient;
    }

    public OneRosterUserResponseDTO getAllStudents() {
        return webClient.get().
                uri(baseUrl + "/ims/oneroster/v1p1/students")
                .retrieve()
                .bodyToMono(OneRosterUserResponseDTO.class)
                .block();
    }

    public OneRosterUserResponseDTO getAllTeachers() {
        return webClient.get().
                uri(baseUrl + "/ims/oneroster/v1p1/teachers")
                .retrieve()
                .bodyToMono(OneRosterUserResponseDTO.class)
                .block();
    }
}
