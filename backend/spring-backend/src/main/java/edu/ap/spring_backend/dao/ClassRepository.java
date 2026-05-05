package edu.ap.spring_backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.ap.spring_backend.jpa.SchoolClass;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<SchoolClass,Long> {
    public List<SchoolClass> findBySchoolId(Long schoolId);
    public Optional<SchoolClass> findByClassNameAndSchoolId(String className, Long schoolId);
}
