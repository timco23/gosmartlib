package edu.ap.spring_backend.specification;

import edu.ap.spring_backend.jpa.Item;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemSpecificationsTest {

    @Test
    void withSearch_null_returnsNullPredicate() {
        assertNull(ItemSpecifications.withSearch(null).toPredicate(null, null, null));
        assertNull(ItemSpecifications.withSearch("  ").toPredicate(null, null, null));
    }

    @Test
    void withMinPages_returnsPredicate() {
        Root<Item> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<Integer> pageCount = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Integer>get("pageCount")).thenReturn(pageCount);
        when(cb.greaterThanOrEqualTo(pageCount, 10)).thenReturn(predicate);

        Predicate result = ItemSpecifications.withMinPages(10).toPredicate(root, query, cb);

        assertSame(predicate, result);
    }

    @Test
    void withLanguages_returnsPredicate() {
        Root<Item> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<String> lang = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get("bookLanguage")).thenReturn(lang);
        when(lang.in(List.of("EN"))).thenReturn(predicate);

        Predicate result = ItemSpecifications.withLanguages(List.of("EN")).toPredicate(root, query, cb);

        assertSame(predicate, result);
    }

    @Test
    void withDidactic_returnsPredicate() {
        Root<Item> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        @SuppressWarnings("unchecked")
        Path<Boolean> didactic = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Boolean>get("didactic")).thenReturn(didactic);
        when(cb.equal(didactic, true)).thenReturn(predicate);

        Predicate result = ItemSpecifications.withDidactic(true).toPredicate(root, query, cb);

        assertSame(predicate, result);
    }
}