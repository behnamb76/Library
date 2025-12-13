package ir.bahman.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnBookRequest {
    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "Member ID is required to verify identity")
    private Long memberId;

    @NotNull(message = "Book Copy ID is required to verify the book")
    private Long bookCopyId;
}
