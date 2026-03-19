package edu.ap.spring_backend.jpa;

/**
 * Enum representing user roles in the application.
 * ADMIN: Administrator with full access (manually created)
 * BIBLIOTHEEKBEHEERDEER: Library manager with extended permissions (manually created)
 * LEERKRACHT: Teacher with moderate permissions (from Smartschool)
 * LEERLING: Student with limited access (from Smartschool)
 */
public enum Role {
    ADMIN, LEERLING
}