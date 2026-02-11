package edu.ap.spring_backend.DAO;

import edu.ap.spring_backend.entity.Hello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelloWorldRepository extends JpaRepository<Hello, Integer> {
}
