package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter
@Setter
@Table(name = "school")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 200)
    private String address;

    private int lendingPeriodDays;
    private int extendPeriodDays;
    private int maxExtends;
    private int maxLendsStudent;
    private int maxLendTeacher;
    private int lenderNotificationPeriod;
    private int lenderLateNotificationPeriod;
    private int librarianNotificationNotReturned;

    private boolean librarianRequestNotification;
    private boolean externalVisibility;
    private boolean externalLending;
    private boolean reviews;
    private boolean reviewModeration;
    private boolean dashboardShowBookOfMonth = true;
    private boolean dashboardShowLastBorrowed = true;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Campus> campuses = new ArrayList<>();

    public School() {
    }

    public void addCampus(Campus campus) {
        campuses.add(campus);
        campus.setSchool(this);
    }

    public void removeCampus(Campus campus) {
        campuses.remove(campus);
        campus.setSchool(null);
    }
}