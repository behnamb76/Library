package ir.bahman.library.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BorrowBookRequest {
    @NotNull(message = "Member id is required!")
    @Positive(message = "Member id must be a positive number")
    private Long memberId;

    @NotNull(message = "Book copy id is required!")
    @Positive(message = "Book copy id must be a positive number")
    private Long bookCopyId;
}
