package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.model.enums.BookCopyStatus;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BookCopy extends BaseEntity<Long> {
    private UUID barcode;
    private BookCopyStatus status;
}
