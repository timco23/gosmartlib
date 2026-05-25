package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.User;
import edu.ap.spring_backend.jpa.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findBySmartschoolUsername(String smartschoolUsername);
    Optional<User> findBySmartschoolUsernameIgnoreCase(String smartschoolUsername);
    Optional<User> findByRefreshToken(String refreshToken);

    List<User> findBySchoolId(Long schoolId);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.classes c WHERE u.school.id = :schoolId " +
           "AND (:filter IS NULL OR LOWER(CONCAT(u.firstName, ' ', u.name)) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :filter, '%')) " +
           "OR LOWER(c.className) LIKE LOWER(CONCAT('%', :filter, '%')))")
    Page<User> findBySchoolIdAndFilter(@Param("schoolId") Long schoolId, @Param("filter") String filter, Pageable pageable);

    boolean existsByEmail(String email);
    boolean existsBySmartschoolUsername(String smartschoolUsername);
    boolean existsByEmailAndIdNot(String email, Integer id);    
    List<User> findByDeletionRequestedAtBefore(LocalDateTime cutoff);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user WHERE id = :id", nativeQuery = true)
    void deleteUserByIdNative(@Param("id") Integer id);
     
    List<User> findBySchool_IdAndRoleIn(Long schoolId, List<Role> roles);

    List<User> findBySchool_IdAndWishlistedBooks_Id(Long schoolId, Long itemId);

    List<User> findBySmartschoolUsernameStartingWith(String prefix);
}