package ir.bahman.library.dto;

import ir.bahman.library.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayPenaltyRequest {
    @NotNull(message = "Penalty id is required!")
    private Long penaltyId;

    @NotNull(message = "Payment method id is required!")
    private String method;
}
