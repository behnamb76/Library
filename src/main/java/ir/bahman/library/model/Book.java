package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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

    private Year publicationYear;

    private String description;

    private Integer edition;

    private BigDecimal replacementCost;

    @OneToMany(mappedBy = "book")
    private List<BookCopy> bookCopies = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(
            name = "book_tags",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "book")
    private List<Reservation> reservations = new ArrayList<>();
}
