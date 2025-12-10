package ir.bahman.library.dto;

import ir.bahman.library.model.enums.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanUpdateRequest {
    @NotNull
    private LocalDateTime loanDate;

    @NotNull
    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    @NotNull
    private LoanStatus status;
}
