package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
public class Location extends BaseEntity<Long> {
    private String section;

    private String shelf;

    private Integer row;

    @OneToMany(mappedBy = "location")
    private List<BookCopy> bookCopies = new ArrayList<>();
}
