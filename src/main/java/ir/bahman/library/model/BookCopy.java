package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.model.enums.BookCopyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookCopy extends BaseEntity<Long> {
    @Column(unique = true)
    private String barcode;

    @Enumerated(EnumType.STRING)
    private BookCopyStatus status;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "bookCopy")
    private List<Loan> loans = new ArrayList<>();
}
