package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the Smartschool "userinfo" response.
 * Maps JSON fields (userID, name, surname, username, platform, role) to Java fields.
 */
public class SmartschoolUserInfo {
    @JsonProperty("userID")
    public String userID;

    @JsonProperty("name")
    public String name;

    @JsonProperty("surname")
    public String surname;

    @JsonProperty("username")
    public String username;

    //If we want to store/verify which Smartschool platform the user came from we can use this.
    @JsonProperty("platform")
    public String platform;

    @JsonProperty("role")
    public String role;
}
