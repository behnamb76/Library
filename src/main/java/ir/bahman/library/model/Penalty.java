package ir.bahman.library.model;

import ir.bahman.library.model.base.BaseEntity;
import ir.bahman.library.model.enums.PenaltyReason;
import ir.bahman.library.model.enums.PenaltyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Penalty extends BaseEntity<Long> {
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PenaltyReason reason;

    @Enumerated(EnumType.STRING)
    private PenaltyStatus status;

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    @OneToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}
