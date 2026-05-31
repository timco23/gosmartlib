package edu.ap.spring_backend.service;

import edu.ap.spring_backend.dao.ClassRepository;
import edu.ap.spring_backend.dao.SchoolRepository;
import edu.ap.spring_backend.dao.UserRepository;
import edu.ap.spring_backend.dto.OneRosterEnrollmentDTO;
import edu.ap.spring_backend.dto.OneRosterEnrollmentsResponseDTO;
import edu.ap.spring_backend.dto.OneRosterUserDTO;
import edu.ap.spring_backend.dto.OneRosterUserResponseDTO;
import edu.ap.spring_backend.jpa.Role;
import edu.ap.spring_backend.jpa.School;
import edu.ap.spring_backend.jpa.SchoolClass;
import edu.ap.spring_backend.jpa.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class OneRosterSyncScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OneRosterSyncScheduler.class);

    private final OneRosterOAuthService oneRosterOAuthService;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final SchoolRepository schoolRepository;

    public OneRosterSyncScheduler(OneRosterOAuthService oneRosterOAuthService,
                                  UserRepository userRepository,
                                  ClassRepository classRepository,
                                  SchoolRepository schoolRepository) {
        this.oneRosterOAuthService = oneRosterOAuthService;
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * Scheduled task to synchronize users and class enrollments every 24 hours.
     * Cron expression: "0 0 0 * * ?" (Midnight every day)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void syncAll() {
        logger.info("Starting scheduled OneRoster synchronization...");

        // Phase 1: Sync User Metadata (School and OneRoster ID)
        syncUserMetadata();

        // Phase 2: Sync Class Enrollments
        syncClassEnrollments();

        logger.info("OneRoster synchronization completed.");
    }

    private void syncUserMetadata() {
        logger.info("Phase 1: Syncing user metadata (Schools and OneRoster IDs)...");
        
        List<OneRosterUserDTO> allStudents = new ArrayList<>();
        List<OneRosterUserDTO> allTeachers = new ArrayList<>();
        
        try {
            OneRosterUserResponseDTO studentResponse = oneRosterOAuthService.getAllStudents();
            if (studentResponse != null && studentResponse.users() != null) {
                allStudents.addAll(Arrays.asList(studentResponse.users()));
            }
            
            OneRosterUserResponseDTO teacherResponse = oneRosterOAuthService.getAllTeachers();
            if (teacherResponse != null && teacherResponse.users() != null) {
                allTeachers.addAll(Arrays.asList(teacherResponse.users()));
            }
        } catch (Exception e) {
            logger.error("Failed to fetch users from OneRoster. User metadata sync may be incomplete.", e);
            return;
        }

        List<User> localUsers = userRepository.findAll();
        List<School> allSchools = schoolRepository.findAll();

        for (User user : localUsers) {
            if (user.getRole() == Role.ADMIN) continue;

            List<OneRosterUserDTO> orPool = (user.getRole() == Role.LEERLING) ? allStudents : allTeachers;
            
            linkOneRosterId(user, orPool);
            
            if (user.getOnerosterId() != null) {
                orPool.stream()
                    .filter(orUser -> orUser.sourcedId().equals(user.getOnerosterId()))
                    .findFirst()
                    .ifPresent(orUser -> linkSchool(user, orUser, allSchools));
            }

            userRepository.save(user);
        }
    }

    private void linkOneRosterId(User user, List<OneRosterUserDTO> orPool) {
        List<OneRosterUserDTO> matches = orPool.stream()
                .filter(orUser -> Objects.equals(orUser.givenName(), user.getFirstName()) && 
                                  Objects.equals(orUser.familyName(), user.getName()))
                .toList();

        if (matches.size() == 1) {
            user.setOnerosterId(matches.get(0).sourcedId());
        } else if (matches.size() > 1) {
            // If multiple matches by name, try to narrow down by email if possible
            matches.stream()
                .filter(orUser -> orUser.email() != null && orUser.email().equalsIgnoreCase(user.getEmail()))
                .findFirst()
                .ifPresent(match -> user.setOnerosterId(match.sourcedId()));
        }
    }

    private void linkSchool(User user, OneRosterUserDTO orUser, List<School> allSchools) {
        if (orUser.orgs() != null && orUser.orgs().length > 0) {
            String orSchoolName = orUser.orgs()[0].name();
            String normalizedOrName = normalizeSchoolName(orSchoolName);

            if (user.getSchool() == null || !normalizeSchoolName(user.getSchool().getName()).equals(normalizedOrName)) {
                allSchools.stream()
                        .filter(s -> normalizeSchoolName(s.getName()).equals(normalizedOrName))
                        .findFirst()
                        .ifPresentOrElse(user::setSchool, () -> user.setSchool(null));
                
                if (user.getSchool() != null) {
                    logger.info("Updated school for user {} to {}", user.getEmail(), user.getSchool().getName());
                }
            }
        }
    }

    private String normalizeSchoolName(String name) {
        if (name == null) return "";
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private void syncClassEnrollments() {
        logger.info("Phase 2: Syncing class enrollments...");

        List<User> allUsers = userRepository.findAll();
        
        // Accumulates all enrollments across all schools during this specific run
        Map<String, Set<String>> enrollmentCache = new HashMap<>();
        Set<Long> fetchedSchoolIds = new HashSet<>();

        for (User user : allUsers) {
            if (user.getRole() == Role.ADMIN || user.getOnerosterId() == null || user.getSchool() == null) {
                continue;
            }

            try {
                Set<String> expectedClassNames = getExpectedClassNames(user, enrollmentCache, fetchedSchoolIds);
                syncUserClasses(user, expectedClassNames);
            } catch (Exception e) {
                logger.error("Failed to sync classes for user: {} (ID: {}). Skipping.", user.getEmail(), user.getId(), e);
            }
        }

        cleanupEmptyClasses();
    }

    private Set<String> getExpectedClassNames(User user, Map<String, Set<String>> cache, Set<Long> fetchedSchoolIds) {
        Long schoolId = user.getSchool().getId();
        String onerosterId = user.getOnerosterId();

        if (fetchedSchoolIds.contains(schoolId)) {
            return cache.getOrDefault(onerosterId, Collections.emptySet());
        }

        try {
            logger.info("Fetching OneRoster enrollments for school: {} using user: {}", user.getSchool().getName(), user.getEmail());
            OneRosterEnrollmentsResponseDTO response = oneRosterOAuthService.getEnrollmentsForSchool(onerosterId);
            
            if (response != null && response.enrollments() != null) {
                for (OneRosterEnrollmentDTO enrollment : response.enrollments()) {
                    if (enrollment.user() != null && enrollment.user().sourcedId() != null && enrollment.enrollmentClass() != null) {
                        String fetchedUserId = enrollment.user().sourcedId();
                        String className = enrollment.enrollmentClass().title();
                        
                        cache.computeIfAbsent(fetchedUserId, k -> new HashSet<>()).add(className);
                    }
                }
            }
            
            fetchedSchoolIds.add(schoolId);
            return cache.getOrDefault(onerosterId, Collections.emptySet());
            
        } catch (Exception e) {
            logger.warn("Could not fetch enrollments for school {} via user {} from OneRoster: {}", 
                    user.getSchool().getName(), user.getEmail(), e.getMessage());
            return Collections.emptySet();
        }
    }

    private void syncUserClasses(User user, Set<String> expectedClassNames) {
        School school = user.getSchool();

        // Remove user from classes they are no longer in
        List<SchoolClass> currentClasses = new ArrayList<>(user.getClasses());
        for (SchoolClass clazz : currentClasses) {
            if (!expectedClassNames.contains(clazz.getClassName())) {
                clazz.getUsers().removeIf(u -> Objects.equals(u.getId(), user.getId()));
                user.getClasses().remove(clazz);
                classRepository.save(clazz);
                logger.debug("Removed user {} from class {}", user.getEmail(), clazz.getClassName());
            }
        }

        // Add user to new classes, create class if it doesn't exist
        Set<String> currentClassNames = user.getClasses().stream()
                .map(SchoolClass::getClassName)
                .collect(Collectors.toSet());

        for (String expectedName : expectedClassNames) {
            if (!currentClassNames.contains(expectedName)) {
                SchoolClass clazz = classRepository.findByClassNameAndSchoolId(expectedName, school.getId())
                        .orElseGet(() -> {
                            SchoolClass newClass = new SchoolClass();
                            newClass.setClassName(expectedName);
                            newClass.setSchool(school);
                            newClass.setUsers(new ArrayList<>());
                            logger.info("Creating new class: {} for school: {}", expectedName, school.getName());
                            return classRepository.save(newClass);
                        });

                if (clazz.getUsers().stream().noneMatch(u -> Objects.equals(u.getId(), user.getId()))) {
                    clazz.getUsers().add(user);
                    user.getClasses().add(clazz);
                    classRepository.save(clazz);
                    logger.debug("Added user {} to class {}", user.getEmail(), expectedName);
                }
            }
        }

        userRepository.save(user);
    }

    private void cleanupEmptyClasses() {
        List<SchoolClass> emptyClasses = classRepository.findEmptyClasses();
        if (!emptyClasses.isEmpty()) {
            logger.info("Cleaning up {} empty classes.", emptyClasses.size());
            classRepository.deleteAll(emptyClasses);
        }
    }
}
