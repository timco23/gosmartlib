package edu.ap.spring_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartschoolUserInfo {
    @JsonProperty("userID")
    private String userID;

    @JsonProperty("username")
    private String username;

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("basisrol")
    private String basisrol;

    @JsonProperty("isCoAccount")
    private Integer isCoAccount;

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

        public String getBasisrol() { return basisrol; }
    public void setBasisrol(String basisrol) { this.basisrol = basisrol; }

    public Integer getIsCoAccount() { return isCoAccount; }
    public void setIsCoAccount(Integer isCoAccount) { this.isCoAccount = isCoAccount; }
}
