package edu.ap.spring_backend.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "reading_list")
public class ReadingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "reading_list_item")
    private List<Item> items = new ArrayList<>();

    public ReadingList() {}

    public ReadingList(String title, String description, User user, List<Item> items) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.items = items;
    }
}
