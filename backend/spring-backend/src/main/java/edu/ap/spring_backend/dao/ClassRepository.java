package edu.ap.spring_backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.Class;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class,Long> {
    public List<Class> findBySchoolId(Long schoolId);
    public Optional<Class> findByClassNameAndSchoolId(String className, Long schoolId);
}
