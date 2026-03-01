package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "school")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
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

    protected School() {
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
}