package ir.bahman.library.dto;

import ir.bahman.library.model.enums.LoanStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDTO {
    private LocalDateTime loanDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    private LoanStatus status;

    private Long memberId;

    private Long bookCopyId;
}
