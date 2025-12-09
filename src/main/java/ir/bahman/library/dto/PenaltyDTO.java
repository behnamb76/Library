package ir.bahman.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenaltyDTO {
    private BigDecimal amount;

    private String reason;

    private String status;

    private LocalDateTime lastCalculatedAt;
}
