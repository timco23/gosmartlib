    package edu.ap.spring_backend.jpa;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    @Entity
    @Table(name = "school_item_copy")
    public class SchoolItemCopy {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private String barcode;
        private boolean featured;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "item_id")
        private Item item;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "school_id")
        private School school;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "campus_id")
        private Campus campus;

        @Enumerated(EnumType.STRING)
        private CopyStatus copy_status;

        @Enumerated(EnumType.STRING)
        @Column(name = "item_condition")
        private ItemCondition condition;

        public SchoolItemCopy() {
        }

        public SchoolItemCopy(String barcode, boolean featured, Item item, School school, Campus campus, CopyStatus copy_status, ItemCondition condition) {
            this.barcode = barcode;
            this.featured = featured;
            this.item = item;
            this.school = school;
            this.campus = campus;
            this.copy_status = copy_status;
            this.condition = condition;
        }
    }
