package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dto.OneRosterEnrollmentsResponseDTO;
import edu.ap.spring_backend.dto.OneRosterUserDTO;
import edu.ap.spring_backend.dto.OneRosterUserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OneRosterOAuthService {
    private final WebClient webClient;

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

    //Using a user's sourcedId will give the enrollments of the school they're in
    public OneRosterEnrollmentsResponseDTO getEnrollmentsForSchool(String schoolId) {
        return webClient.get().
                uri(baseUrl + "/ims/oneroster/v1p1/schools/" + schoolId + "/enrollments")
                .retrieve()
                .bodyToMono(OneRosterEnrollmentsResponseDTO.class)
                .block();
    }
}
