package edu.ap.spring_backend.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SchoolItemCopy {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    private int schoolItemId;
    private String barcode;
    private String status;
    private String condition;

    protected SchoolItemCopy() {
    }

    public SchoolItemCopy(int schoolItemId, String barcode, String status, String condition) {
        this.schoolItemId = schoolItemId;
        this.barcode = barcode;
        this.status = status;
        this.condition = condition;
    }
}
