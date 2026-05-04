package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findBySmartschoolUsername(String smartschoolUsername);
    Optional<User> findBySmartschoolUsernameIgnoreCase(String smartschoolUsername);
    Optional<User> findByRefreshToken(String refreshToken);

    List<User> findBySchoolId(Long schoolId);

    boolean existsByEmail(String email);
    boolean existsBySmartschoolUsername(String smartschoolUsername);
    boolean existsByEmailAndIdNot(String email, Integer id);    
}