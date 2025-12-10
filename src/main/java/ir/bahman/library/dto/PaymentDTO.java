package ir.bahman.library.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    private String method;

    private String paymentFor;

    private Long memberId;
}
