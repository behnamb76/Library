package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Book extends BaseEntity<Long> {
    private String title;

    private String author;

    @Column(unique = true)
    private String isbn;

    private String publisher;

    private Integer publicationYear;

    private String description;

    private Integer edition;
}
