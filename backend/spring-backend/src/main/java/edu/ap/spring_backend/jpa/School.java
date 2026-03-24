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

    // Campussen lijst
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Campus> campuses = new ArrayList<>();


    public School() {
    }

    public School(String name, String address,
            int lendingPeriodDays,
            int extendPeriodDays,
            int maxExtends,
            int maxLendsStudent,
            int maxLendTeacher,
            int lenderNotificationPeriod,
            int lenderLateNotificationPeriod,
            int librarianNotificationNotReturned,
            boolean librarianRequestNotification,
            boolean externalVisibility,
            boolean externalLending,
            boolean reviews,
            boolean reviewModeration) {

        this.name = name;
        this.address = address;
        this.lendingPeriodDays = lendingPeriodDays;
        this.extendPeriodDays = extendPeriodDays;
        this.maxExtends = maxExtends;
        this.maxLendsStudent = maxLendsStudent;
        this.maxLendTeacher = maxLendTeacher;
        this.lenderNotificationPeriod = lenderNotificationPeriod;
        this.lenderLateNotificationPeriod = lenderLateNotificationPeriod;
        this.librarianNotificationNotReturned = librarianNotificationNotReturned;
        this.librarianRequestNotification = librarianRequestNotification;
        this.externalVisibility = externalVisibility;
        this.externalLending = externalLending;
        this.reviews = reviews;
        this.reviewModeration = reviewModeration;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getLendingPeriodDays() {
        return lendingPeriodDays;
    }

    public int getExtendPeriodDays() {
        return extendPeriodDays;
    }

    public int getMaxExtends() {
        return maxExtends;
    }

    public int getMaxLendsStudent() {
        return maxLendsStudent;
    }

    public int getMaxLendTeacher() {
        return maxLendTeacher;
    }

    public int getLenderNotificationPeriod() {
        return lenderNotificationPeriod;
    }

    public int getLenderLateNotificationPeriod() {
        return lenderLateNotificationPeriod;
    }

    public int getLibrarianNotificationNotReturned() {
        return librarianNotificationNotReturned;
    }

    public boolean getLibrarianRequestNotification() {
        return librarianRequestNotification;
    }

    public boolean isExternalVisibility() {
        return externalVisibility;
    }

    public boolean isExternalLending() {
        return externalLending;
    }

    public boolean isReviews() {
        return reviews;
    }

    public boolean isReviewModeration() {
        return reviewModeration;
    }

    // Methods voor campussen
    public void addCampus(Campus campus) {
        campuses.add(campus);
        campus.setSchool(this);
    }

    public void removeCampus(Campus campus) {
        campuses.remove(campus);
        campus.setSchool(null);
    }
}