package edu.ap.spring_backend.DAO;

import edu.ap.spring_backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Integer> {
    boolean existsByTitle(String itemTitle);
    Optional<Item> findByTitle(String itemTitle);
}
