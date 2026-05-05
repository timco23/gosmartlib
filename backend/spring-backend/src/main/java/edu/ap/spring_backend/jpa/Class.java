package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "class")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String className;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "class_user",  joinColumns = @JoinColumn(name = "class_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();;

    public Class() {}

    public Class(String className, School school, List<User> users) {
        this.className = className;
        this.school = school;
        this.users = users;
    }
}
