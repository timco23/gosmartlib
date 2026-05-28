package edu.ap.spring_backend.dao;

import edu.ap.spring_backend.jpa.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,Long>, JpaSpecificationExecutor<Item> {
    boolean existsByTitle(String itemTitle);
    Optional<Item> findByTitle(String itemTitle);
    List<Item> findByUpdatingTrueAndUpdatingAtBefore(LocalDateTime cutoff);
}
