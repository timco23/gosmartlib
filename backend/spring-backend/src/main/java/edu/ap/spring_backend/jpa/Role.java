package edu.ap.spring_backend.jpa;

/**
 * Enum representing user roles in the application.
 * ADMIN: Administrator with full access to all features.
 * BEHEERDER: Manager with access to features that a teacher has alongside managerial features like managing books in the library and handling book purchase requests.
 * LEERKRACHT: Teacher with access to features that a student has alongside exclusive features like seeing exclusive books or making and sharing reading lists.
 * LEERLING: Student with access to basic features like viewing and lending books.

 */
public enum Role {
    ADMIN, LEERLING, LEERKRACHT, BEHEERDER
}