package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import edu.ap.spring_backend.util.EncryptedStringConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false, name = "name")
    private String name;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "first_name")
    private String firstName;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(nullable = true, name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;

    @Column(name = "student_number")
    private String studentNumber;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "users")
    private List<SchoolClass> classes = new ArrayList<>();

    @Column(name = "smartschool_username", unique = true)
    private String smartschoolUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_favorite_books", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> favoriteBooks = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_wishlisted_books", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> wishlistedBooks = new ArrayList<>();

    @Column(name = "items_borrowed")
    private Integer itemsBorrowed = 0;

    @Column(name = "deletion_requested_at")
    private LocalDateTime deletionRequestedAt;

    /**
     * Refresh token for JWT session persistence (our own token, not Smartschool's).
     */
    @Column(name = "refresh_token", length = 1024)
    private String refreshToken;

    /**
     * Smartschool OAuth access token.
     * Short-lived (1 hour). Used to call /Api/V1/sendnotif and /Api/V1/sendmsg.
     */
    @Column(name = "smartschool_access_token", length = 2048)
    private String smartschoolAccessToken;

    public User() {}

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getStudentNumber() { return studentNumber; }
    public List<SchoolClass> getClasses() { return classes; }
    public School getSchool() { return school; }
    public String getSmartschoolUsername() { return smartschoolUsername; }
    public String getRefreshToken() { return refreshToken; }
    public String getSmartschoolAccessToken() { return smartschoolAccessToken; }
    public List<Item> getFavoriteBooks() { return favoriteBooks; }
    public List<Item> getWishlistedBooks() { return wishlistedBooks; }
    public Integer getItemsBorrowed() { return itemsBorrowed; }
    public LocalDateTime getDeletionRequestedAt() { return deletionRequestedAt; }

    public void setName(String name) { this.name = name; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public void setClass(List<SchoolClass> classes) { this.classes = classes; }
    public void setSchool(School school) { this.school = school; }
    public void setSmartschoolUsername(String smartschoolUsername) { this.smartschoolUsername = smartschoolUsername; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setSmartschoolAccessToken(String smartschoolAccessToken) { this.smartschoolAccessToken = smartschoolAccessToken; }
    public void setFavoriteBooks(List<Item> favoriteBooks) { this.favoriteBooks = favoriteBooks; }
    public void setWishlistedBooks(List<Item> wishlistedBooks) { this.wishlistedBooks = wishlistedBooks; }
    public void setItemsBorrowed(Integer itemsBorrowed) { this.itemsBorrowed = itemsBorrowed; }
    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) { this.deletionRequestedAt = deletionRequestedAt; }
}