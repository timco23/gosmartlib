package edu.ap.spring_backend.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.ap.spring_backend.jpa.SchoolItemCopy;

@Repository
public interface SchoolItemCopyRepository extends JpaRepository<SchoolItemCopy, Integer> {

    List<SchoolItemCopy> findBySchoolItemId(int schoolItemId);

}