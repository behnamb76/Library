package ir.bahman.library.dto;

import ir.bahman.library.model.enums.BookCopyStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookCopyDTO {
    private String barcode;

    private BookCopyStatus status;

    private Long bookId;

    private Long locationId;
}
